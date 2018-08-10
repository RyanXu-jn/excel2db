package com.ryantsui.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by xufy on 2018/3/12.
 */
public class HttpClient {
    /**
     *  请求发起类
     * @param url 路径
     * @param data 数据
     * @return 返回值
     * @throws Exception 异常处理
     */
    public static String connectionByUrl(String url,String data) throws Exception{
        BufferedReader in=null;
        try {
            URL u=new URL(url);
            HttpURLConnection con=(HttpURLConnection)u.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept-Charset", "UTF-8");
            con.setRequestProperty("Content-Type", "application/json");
            con.setConnectTimeout(10000);
            if (StringUtils.isNotBlank(data)) {
                con.connect();
                DataOutputStream out = new DataOutputStream(con.getOutputStream());
                out.write(data.getBytes());
                out.flush();
                out.close();
            }
            in = new BufferedReader(new InputStreamReader(
                    con.getInputStream(),"UTF-8"));
            StringBuilder json = new StringBuilder();
            String inputLine = null;
            while ( (inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            return json.toString();
        } catch (Exception e) {
            throw e;
        }finally{
            try {
                if (in!=null) {
                    in.close();
                }
            } catch (IOException e1) {
            }
        }
    }

    /**
     * 根据url预览返回的数据.
     * @param url 请求地址
     * @param type 请求类型
     * @param property 请求属性
     * @param data 请求数据
     * @return String
     * @throws Exception 异常
     */
    public static String previewDataByUrl(String url,String type,String property,String data) throws Exception{
        BufferedReader in=null;
        try {
            URL u=new URL(url);
            HttpURLConnection con=(HttpURLConnection)u.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod(type);
            con.setRequestProperty("Accept-Charset", "UTF-8");
            con.setRequestProperty("Content-Type", property);
            con.setConnectTimeout(10000);
            if (StringUtils.isNotBlank(data)) {
                con.connect();
                DataOutputStream out = new DataOutputStream(con.getOutputStream());
                out.write(data.getBytes());
                out.flush();
                out.close();
            }
            in = new BufferedReader(new InputStreamReader(
                    con.getInputStream(),"UTF-8"));
            StringBuilder json = new StringBuilder();
            String inputLine = null;
            while ( (inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            return json.toString();
        } catch (Exception e) {
            throw e;
        }finally{
            try {
                if (in!=null) {
                    in.close();
                }
            } catch (IOException e2) {
            }
        }
    }
}
