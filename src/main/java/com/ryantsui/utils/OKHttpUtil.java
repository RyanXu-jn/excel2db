package com.ryantsui.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp3请求类.
 *
 * @auther Ryan Xu
 * @date 2019/10/19 10:33
 */
public class OKHttpUtil {
	private static final Logger logger = LoggerFactory.getLogger(OKHttpUtil.class);
	private static final ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	//1.
	// OkHttpClient client = new OkHttpClient();
	//2
	static OkHttpClient client = new OkHttpClient.Builder()
			.readTimeout(30, TimeUnit.SECONDS)
			.build();
	//3
	//OkHttpClient client = new OkHttpClient().newBuilder().build();
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private static final MediaType TEXT = MediaType.parse("text/plain; charset=utf-8");
	private static final MediaType XML = MediaType.parse("text/xml; chartset=utf-8");

	public static String previewDataByUrl(String url,String type,String property,String data) throws Exception{
		String responseStr = "请求失败，请确认地址和参数信息！";
		Request request = null;
		//get请求
		if ("get".equalsIgnoreCase(type)) {
			request = new Request.Builder()
					.url(url)
					.build();
		} else {
			//post请求
			Request.Builder builder = new Request.Builder().url(url);
			if ("x-www-form-urlencoded".equals(property)) {
				FormBody.Builder formBuilder = new FormBody.Builder();
				if (StringUtils.isNotBlank(data)) {
					Map<String, String> param = objectMapper.readValue(data, new TypeReference<Map<String, String>>() {});
					param.forEach(formBuilder::add);
				}
				RequestBody requestBody = formBuilder.build();
				builder.post(requestBody);
			} else if ("application/json".equals(property)){
				if (StringUtils.isNotBlank(data)) {
					RequestBody requestBody = RequestBody.create(JSON, data);
					builder.post(requestBody);
				}
			} else if ("application/xml".equals(property)){
				if (StringUtils.isNotBlank(data)) {
					RequestBody requestBody = RequestBody.create(XML, data);
					builder.post(requestBody);
				}
			} else if ("text/plain".equals(property)) {
				if (StringUtils.isNotBlank(data)) {
					RequestBody requestBody = RequestBody.create(TEXT, data);
					builder.post(requestBody);
				}
			}
			request = builder.build();
		}
		Response response = client.newCall(request).execute();
		Headers responseHeaders = response.headers();
		for (int i = 0, size = responseHeaders.size(); i < size; i++) {
			logger.info(responseHeaders.name(i) + ": " + responseHeaders.value(i));
		}
		responseStr = response.body().string();
		logger.info("返回结果:{}", responseStr);
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
