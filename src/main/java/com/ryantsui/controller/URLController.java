package com.ryantsui.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryantsui.entity.JsonMessage;
import com.ryantsui.utils.HttpClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Created by xufy on 2018/8/10.
 */
@Controller
@RequestMapping("url")
public class URLController {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    /**
     * 预览url数据源数据
     * @param data 数据json
     * @return JsonMessage
     * @throws Exception 异常
     */
    @RequestMapping(value="/previewDataUrl",method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String prepreviewDataUrl(String data) throws Exception {
        Map<String,String> param = objectMapper.readValue(data, new TypeReference<Map<String, String>>() {});
        if(null == param){
            throw new Exception("参数为空");
        }
        String result = HttpClient.previewDataByUrl(param.get("requestUrl"),param.get("requestType"),
                param.get("requestProperty"),param.get("requestData"));
        Map<String, Object> object = objectMapper.readValue(result, new TypeReference<Map<String, Object>>(){});
        return objectMapper.writeValueAsString(object);
    }

    /**
     * 保存url数据源.
     * @param data 数据
     * @return JsonMessage
     * @throws Exception 异常
     */
    @RequestMapping(value = "/saveDataUrl",method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public JsonMessage saveDataUrl(String data) throws Exception{
        try {
            Map<String, Object> param = objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {
            });
            if (null == param) {
                return new JsonMessage().failure("参数为空");
            }

            return new JsonMessage().success();
        }catch (Exception ex){
            return new JsonMessage().failure(ex.getMessage());
        }
    }
}
