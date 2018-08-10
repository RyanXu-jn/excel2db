package com.ryantsui.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
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

    /**
     * 预览url数据源数据
     * @param data 数据json
     * @return JsonMessage
     * @throws Exception 异常
     */
    @RequestMapping(value="/previewDataUrl",method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public JsonMessage prepreviewDataUrl(String data) throws Exception {
        Map<String,String> param = JSON.parseObject(data, new TypeReference<Map<String, String>>() {});
        if(null == param){
            return new JsonMessage().failure("参数为空");
        }
        String result = HttpClient.previewDataByUrl(param.get("requestUrl"),param.get("requestType"),
                param.get("requestProperty"),param.get("requestData"));
        JSONObject object = JSON.parseObject(result);
        return new JsonMessage().success(object);
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
            Map<String, Object> param = JSON.parseObject(data, new TypeReference<Map<String, Object>>() {
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
