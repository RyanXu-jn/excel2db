package com.ryantsui.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp3请求类.
 *
 * @auther Ryan Xu
 * @date 2019/10/19 10:33
 */
public class OKHttpUtil {
	private static final Logger logger = LoggerFactory.getLogger(OKHttpUtil.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
			.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	private static final String X_WWW_FORM_URLENCODED = "x-www-form-urlencoded";
	private static final String APPLICATION_JSON = "application/json";
	private static final String APPLICATION_XML = "application/xml";
	private static final String TEXT_PLAIN = "text/plain";
	private static final String MULTIPART_FORM_DATA = "multipart/form-data";

	//1.
	// OkHttpClient client = new OkHttpClient();
	//2
	static OkHttpClient client = new OkHttpClient.Builder()
			.readTimeout(60, TimeUnit.SECONDS)
			.build();
	//3
	//OkHttpClient client = new OkHttpClient().newBuilder().build();
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private static final MediaType TEXT = MediaType.parse("text/plain; charset=utf-8");
	private static final MediaType XML = MediaType.parse("text/xml; chartset=utf-8");
	private static final MediaType MULTI = MediaType.parse("multipart/form-data;charset=utf-8");

	public static String previewDataByUrl(String url, String type, String property,
										  String data, MultipartFile file) throws Exception{
		String responseStr = "请求失败，请确认地址和参数信息！";
		Request request = null;
		//get请求
		if ("get".equalsIgnoreCase(type)) {
			request = new Request.Builder()
					.url(url)
					.build();
		} else {
			//post请求
			Request.Builder requestBuilder = new Request.Builder().url(url);
			if (X_WWW_FORM_URLENCODED.equals(property)) {
				FormBody.Builder formBuilder = new FormBody.Builder();
				if (StringUtils.isNotBlank(data)) {
					Map<String, String> param = OBJECT_MAPPER.readValue(data, new TypeReference<Map<String, String>>() {});
					param.forEach(formBuilder::add);
				}
				RequestBody requestBody = formBuilder.build();
				requestBuilder.post(requestBody);
			} else if (APPLICATION_JSON.equals(property)){
				if (StringUtils.isNotBlank(data)) {
					RequestBody requestBody = RequestBody.create(JSON, data);
					requestBuilder.post(requestBody);
				}
			} else if (APPLICATION_XML.equals(property)){
				if (StringUtils.isNotBlank(data)) {
					RequestBody requestBody = RequestBody.create(XML, data);
					requestBuilder.post(requestBody);
				}
			} else if (TEXT_PLAIN.equals(property)) {
				if (StringUtils.isNotBlank(data)) {
					RequestBody requestBody = RequestBody.create(TEXT, data);
					requestBuilder.post(requestBody);
				}
			} else if (MULTIPART_FORM_DATA.equals(property)){
				MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().
						setType(MultipartBody.FORM);
				if (!file.isEmpty()) {
					multipartBuilder.addFormDataPart("file", file.getOriginalFilename(),
							RequestBody.create(MULTI, file.getBytes()));
				}
				if (StringUtils.isNotBlank(data)) {
					Map<String, String> param = OBJECT_MAPPER.readValue(data, new TypeReference<Map<String, String>>() {});
					param.forEach(multipartBuilder::addFormDataPart);
				}
				RequestBody requestBody = multipartBuilder.build();
				requestBuilder.post(requestBody);
			}
			request = requestBuilder.build();
		}
		Response response = client.newCall(request).execute();
		Headers responseHeaders = response.headers();
		for (int i = 0, size = responseHeaders.size(); i < size; i++) {
			logger.info(responseHeaders.name(i) + ": " + responseHeaders.value(i));
		}
		if (response.isSuccessful()) {
			responseStr = Objects.requireNonNull(response.body()).string();
			logger.info("返回结果:{}", responseStr);
		}
		return responseStr;
		/*异步方法调用
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				logger.error(e.getMessage());
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

				Headers responseHeaders = response.headers();
				for (int i = 0, size = responseHeaders.size(); i < size; i++) {
					logger.info(responseHeaders.name(i) + ": " + responseHeaders.value(i));
				}
				if (response.body() != null) {
					responseStr[0] = response.body().string();
				} else {
					responseStr[0] = "没有返回值";
				}
			}
		});*/
	}
}
