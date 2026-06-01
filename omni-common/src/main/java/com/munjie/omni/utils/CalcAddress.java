/**
 * 默认：
 */

package com.munjie.omni.utils;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.util.UriUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

public class CalcAddress {

    public static String URL = "https://api.map.baidu.com/routematrix/v2/driving?";

    public static String AK = "qgeKGQN9jEKPQQvkQDGAKnG6eeKEwGk0";
    public static String AK_BIG = "qgeKGQN9jEKPQQvkQDGAKnG6eeKEwGk0";
    public static String START_WUXI = "31.498809732685716,120.31858328810601";
    public static String START_SHENYANG = "41.68383006919066,123.47109664482264";

    public static String START_QINDAO = "36.072227496663227,120.38945519114627";
    public static String START_YANGJIANG = "21.864339726138934,111.98848929181269";
    public static String START_SH = "31.235929042252015,121.48053886017651";
    public static String START_SUZHOU = "31.303564074441768,120.59241222959322";



    public static void main(String[] args) throws Exception {
        String dist = getDist(START_SH,"内蒙古呼和浩特市土默特左旗");
        System.out.println("公里数: " + dist);

    }

    public static String getDist(String start,String add){
        String org = null;
        if ("上海市".equals(start)) {
            org = START_SH;
        } else if ("苏州市".equals(start)) {
            org = START_SUZHOU;
        } else if ("阳江市".equals(start)) {
            org = START_YANGJIANG;
        }else if ("青岛市".equals(start)) {
            org = START_QINDAO;
        }  else {
            org = GecodingAddress.getLng(start);
        }
        if (StrUtil.isBlank(org)) {
            return "起始地址错误";
        }
        System.out.println("开始处理-------------------》"+ start + org);
        String distanceText = null;
        String end = GecodingAddress.getLng(add);
        if (StrUtil.isEmpty(end)) {
            return "终点地址错误";
        }
        Map params = new LinkedHashMap<String, String>();
        params.put("origins", org);
        params.put("destinations", end);
        params.put("ak", AK);
        try {
            String jsonString = getDistance(URL, params);
            if (StrUtil.isEmpty(jsonString)) {
                return null;
            }
            JSONObject jsonObject = JSON.parseObject(jsonString);
            JSONArray resultArray = jsonObject.getJSONArray("result");
            JSONObject firstResult = resultArray.getJSONObject(0);
            JSONObject distance = firstResult.getJSONObject("distance");
             distanceText = distance.getString("text");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return distanceText;

    };

    /**
     * 默认ak
     * 选择了ak，使用IP白名单校验：
     * 根据您选择的AK已为您生成调用代码
     * 检测到您当前的ak设置了IP白名单校验
     * 您的IP白名单中的IP非公网IP，请设置为公网IP，否则将请求失败
     * 请在IP地址为xxxxxxx的计算发起请求，否则将请求失败
     */
    public static String getDistance(String strUrl, Map<String, String> param) throws Exception {
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