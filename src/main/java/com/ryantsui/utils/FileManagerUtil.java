package com.ryantsui.utils;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.tus.java.client.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * go-fastdfs 文件上传工具类.
 */
public class FileManagerUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileManagerUtil.class);
    private static final String CONFIG_FILE = "fastdfs.properties";
    private static final String FASTDFS_DOMAIN_KEY = "fastdfs.domain";
    private static final String UPLOAD_URL_KEY = "fastdfs.upload.url";
    private static final String DELETE_URL_KEY = "fastdfs.delete.url";

    private static ThreadLocal<String> tl = new ThreadLocal<>();

    private static String SERVER_DOMAIN;
    private static String UPLOAD_URL;
    private static String DELETE_URL;
    private static TusClient client = new TusClient();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

    static {
        try {
            InputStream in = FileManagerUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            Properties properties = new Properties();
            properties.load(Objects.requireNonNull(in));
            SERVER_DOMAIN = String.valueOf(properties.get(FASTDFS_DOMAIN_KEY));
            UPLOAD_URL = String.valueOf(properties.get(UPLOAD_URL_KEY));
            DELETE_URL = String.valueOf(properties.get(DELETE_URL_KEY));
            Objects.requireNonNull(in).close();
            client.setUploadCreationURL(new URL(SERVER_DOMAIN + "/big" + UPLOAD_URL + "/"));
            client.enableResuming(new TusURLMemoryStore());
        } catch (Exception e) {
            logger.error("加载fastdfs配置文件失败", e);
        }
    }

    /**
     * 文件上传.
     *
     * @param file file
     * @param data string
     * @return string
     * @throws Exception exception
     */
    public static String upload(MultipartFile file, String data) throws Exception {
        return OKHttpUtil.previewDataByUrl(SERVER_DOMAIN + UPLOAD_URL, "post", MediaType.MULTIPART_FORM_DATA_VALUE, data, file);
    }

    /**
     * tus文件断点上传.
     *
     * @param file file
     * @param data string
     * @return string
     * @throws Exception exception
     */
    public static String tusFileUpload(MultipartFile file, String data) throws Exception {
        try {
            System.setProperty("http.strictPostRedirect", "true");
            TusUpload upload = new TusUpload();
            upload.setInputStream(file.getInputStream());
            upload.setSize(file.getSize());
            upload.setFingerprint("stream");
            upload.setMetadata(new HashMap<>());
            upload.getMetadata().put("filename", file.getOriginalFilename());
            logger.info("Starting upload...");
            TusExecutor executor = new TusExecutor() {
                @Override
                protected void makeAttempt() throws ProtocolException, IOException {
                    // First try to resume an upload. If that's not possible we will create a new
                    // upload and get a TusUploader in return. This class is responsible for opening
                    // a connection to the remote server and doing the uploading.
                    TusUploader uploader = client.resumeOrCreateUpload(upload);

                    // Upload the file in chunks of 500KB sizes.
                    uploader.setChunkSize(512000);

                    // Upload the file as long as data is available. Once the
                    // file has been fully uploaded the method will return -1
                    do {
                        // Calculate the progress using the total size of the uploading file and
                        // the current offset.
                        long totalBytes = upload.getSize();
                        long bytesUploaded = uploader.getOffset();
                        double progress = (double) bytesUploaded / totalBytes * 100;

                        System.out.printf("Upload at %06.2f%%.\n", progress);
                    } while (uploader.uploadChunk() > -1);

                    // Allow the HTTP connection to be closed and cleaned up
                    uploader.finish();
                    while (true) {
                        if (StringUtils.isNotBlank(uploader.getUploadURL().toString())) {
                            logger.info("Upload finished.");
                            tl.set(uploader.getUploadURL().toString());
                            logger.info("Upload available at:{}", tl.get());
                            break;
                        }
                    }
                }
            };
            executor.makeAttempts();
            String realUrl = SERVER_DOMAIN + UPLOAD_URL + "?output=json&md5=" + tl.get().substring(tl.get().lastIndexOf("/") + 1);
            logger.info("file real url：{}", realUrl);
            Map<String, Object> json;
            while (true) {
                String jsonStr = OKHttpUtil.previewDataByUrl(realUrl, "get", MediaType.MULTIPART_FORM_DATA_VALUE, data, null);
                json = objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
                if (StringUtils.isNotBlank(String.valueOf(json.get("url")))) {
                    break;
                }
                TimeUnit.SECONDS.sleep(1);
            }
            logger.info("最终路径:{}", json.get("url"));
            return String.valueOf(json.get("url"));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }


    /**
     * 删除文件.
     *
     * @param path path
     * @throws Exception exception
     */
    public static void delete(String path) throws Exception {
        int groupIndex = path.indexOf("group");
        String filePath = path.substring(groupIndex - 1);
        OKHttpUtil.previewDataByUrl(SERVER_DOMAIN + DELETE_URL, "post", MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                "{\"path\":\"" + filePath + "\"}", null);
    }
//    public static void main(String[] args) {
//        try {
//            FileManagerUtil.delete("http://49.233.208.246:7080/group1/default/20200423/18/13/5/200423181334745.jpg");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

   /* public static void main(String[] args) {
        try {
            // When Java's HTTP client follows a redirect for a POST request, it will change the
            // method from POST to GET which can be disabled using following system property.
            // If you do not enable strict redirects, the tus-java-client will not follow any
            // redirects but still work correctly.
            System.setProperty("http.strictPostRedirect", "true");

            // Create a new TusClient instance
            final TusClient client = new TusClient();

            // Configure tus HTTP endpoint. This URL will be used for creating new uploads
            // using the Creation extension
            client.setUploadCreationURL(new URL( "http://49.233.208.246:7080/group1/big/upload/"));
            //client.setUploadCreationURL(new URL( "https://master.tus.io/files/"));

            // Enable resumable uploads by storing the upload URL in memory
            client.enableResuming(new TusURLMemoryStore());

            // Open a file using which we will then create a TusUpload. If you do not have
            // a File object, you can manually construct a TusUpload using an InputStream.
            // See the documentation for more information.
            File file = new File("D:\\上海奉贤智慧厅\\智能咨询\\奉贤视频\\goods.png");
            final TusUpload upload = new TusUpload(file);

            // You can also upload from an InputStream directly using a bit more work:
            // InputStream stream = …;
            // TusUpload upload = new TusUpload();
            // upload.setInputStream(stream);
            // upload.setSize(sizeOfStream);
            // upload.setFingerprint("stream");


            System.out.println("Starting upload...");

            // We wrap our uploading code in the TusExecutor class which will automatically catch
            // exceptions and issue retries with small delays between them and take fully
            // advantage of tus' resumability to offer more reliability.
            // This step is optional but highly recommended.
            TusExecutor executor = new TusExecutor() {
                @Override
                protected void makeAttempt() throws ProtocolException, IOException {
                    // First try to resume an upload. If that's not possible we will create a new
                    // upload and get a TusUploader in return. This class is responsible for opening
                    // a connection to the remote server and doing the uploading.
                    TusUploader uploader = client.resumeOrCreateUpload(upload);

                    // Upload the file in chunks of 1KB sizes.
                    uploader.setChunkSize(1024);

                    // Upload the file as long as data is available. Once the
                    // file has been fully uploaded the method will return -1
                    do {
                        // Calculate the progress using the total size of the uploading file and
                        // the current offset.
                        long totalBytes = upload.getSize();
                        long bytesUploaded = uploader.getOffset();
                        double progress = (double) bytesUploaded / totalBytes * 100;

                        System.out.printf("Upload at %06.2f%%.\n", progress);
                    } while(uploader.uploadChunk() > -1);

                    // Allow the HTTP connection to be closed and cleaned up
                    uploader.finish();

                    System.out.println("Upload finished.");
                    System.out.format("Upload available at: %s", uploader.getUploadURL().toString());
                }
            };
            executor.makeAttempts();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }*/
}
