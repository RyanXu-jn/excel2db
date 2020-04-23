package com.ryantsui.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * go-fastdfs 文件上传工具类.
 */
public class FileManagerUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileManagerUtil.class);
    private static final String CONFIG_FILE = "fastdfs.properties";
    private static final String FASTDFS_DOMAIN_KEY = "fastdfs.domain";
    private static final String UPLOAD_URL_KEY = "fastdfs.upload.url";
    private static final String DELETE_URL_KEY = "fastdfs.delete.url";

    private static String SERVER_DOMAIN;
    private static String UPLOAD_URL;
    private static String DELETE_URL;
    static {
        try {
            InputStream in = FileManagerUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            Properties properties = new Properties();
            properties.load(Objects.requireNonNull(in));
            SERVER_DOMAIN = String.valueOf(properties.get(FASTDFS_DOMAIN_KEY));
            UPLOAD_URL = String.valueOf(properties.get(UPLOAD_URL_KEY));
            DELETE_URL = String.valueOf(properties.get(DELETE_URL_KEY));
            Objects.requireNonNull(in).close();
        } catch (Exception e) {
            logger.error("加载fastdfs配置文件失败",e);
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
        return OKHttpUtil.previewDataByUrl(SERVER_DOMAIN + UPLOAD_URL, "post",MediaType.MULTIPART_FORM_DATA_VALUE, data, file);
    }


    /**
     * 删除文件.
     * @param path path
     * @throws Exception exception
     */
    public static void delete(String path) throws Exception {
        int groupIndex = path.indexOf("group");
        String filePath = path.substring(groupIndex - 1);
        OKHttpUtil.previewDataByUrl(SERVER_DOMAIN + DELETE_URL,"post", MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                "{\"path\":\"" + filePath + "\"}",null);
    }
    public static void main(String[] args) {
        try {
            FileManagerUtil.delete("http://78.120.16.145:8088/group1/default/20200423/18/13/5/200423181334745.jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
