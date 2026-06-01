/**
  * 默认经纬度计算：
  */

package com.munjie.omni.utils;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.util.UriUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

public class GecodingAddress {

    public static String URL = "https://api.map.baidu.com/geocoding/v3?";

    public static String AK = "qgeKGQN9jEKPQQvkQDGAKnG6eeKEwGk0";

    public static void main(String[] args) throws Exception {

        Map params = new LinkedHashMap<String, String>();
        params.put("address", "苏州市");
        params.put("output", "json");
        params.put("ak", AK);
//        params.put("callback", "showLocation");


        String s = requestGetAK(URL, params);
        System.out.println("AK: " + s);
    }

    public static String getLng(String add) {
        Map params = new LinkedHashMap<String, String>();
        params.put("address", add);
        params.put("output", "json");
        params.put("ak", AK);
        try {
            String jsonString = requestGetAK(URL, params);
            if (StrUtil.isNotEmpty(jsonString)) {
                JSONObject jsonObject = JSON.parseObject(jsonString);
                JSONObject result = jsonObject.getJSONObject("result");
                Integer status = jsonObject.getInteger("status");
                if (status != 0) {
                    return StrUtil.EMPTY;
                }
                JSONObject location = result.getJSONObject("location");
                double lng = location.getDoubleValue("lng");
                double lat = location.getDoubleValue("lat");
                return lat + "," + lng;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return StrUtil.EMPTY;
        }
        return StrUtil.EMPTY;
    }

    /**
     * 默认ak
     * 选择了ak，使用IP白名单校验：
     * 根据您选择的AK已为您生成调用代码
     * 检测到您当前的ak设置了IP白名单校验
     * 您的IP白名单中的IP非公网IP，请设置为公网IP，否则将请求失败
     * 请在IP地址为xxxxxxx的计算发起请求，否则将请求失败
     */
    public static String requestGetAK(String strUrl, Map<String, String> param) throws Exception {
        SSLUtil.ignoreSSL();
        if (strUrl == null || strUrl.length() <= 0 || param == null || param.size() <= 0) {
            return null;
        }

        StringBuffer queryString = new StringBuffer();
        queryString.append(strUrl);
        for (Map.Entry<?, ?> pair : param.entrySet()) {
            queryString.append(pair.getKey() + "=");
            //    第一种方式使用的 jdk 自带的转码方式  第二种方式使用的 spring 的转码方法 两种均可
            //    queryString.append(URLEncoder.encode((String) pair.getValue(), "UTF-8").replace("+", "%20") + "&");
            queryString.append(UriUtils.encode((String) pair.getValue(), "UTF-8") + "&");
        }

        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }

        URL url = new URL(queryString.toString());
        System.out.println(queryString.toString());
        URLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.connect();

        InputStreamReader isr = new InputStreamReader(httpConnection.getInputStream());
        BufferedReader reader = new BufferedReader(isr);
        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        isr.close();
        return buffer.toString();
    }
}