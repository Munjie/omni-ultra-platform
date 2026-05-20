package com.munjie.omni.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;


@Component
@Slf4j
public class LoginHttpUtil {

    @Value("${spring.profiles.active}")
    private String activeProfile;
    /**
     * 探测连通性
     */
    public boolean isReachable(String url) {
        HttpHead httpHead = new HttpHead(url);
        httpHead.setConfig(getRequestConfig());
        try (CloseableHttpClient httpClient = createHttpClient();
             CloseableHttpResponse response = httpClient.execute(httpHead)) {
            int statusCode = response.getStatusLine().getStatusCode();
            return statusCode >= 200 && statusCode < 400;
        } catch (Exception e) {
            log.warn("{}连通性预检失败: {}", url, e.getMessage());
            return false;
        }
    }



    public CloseableHttpClient createHttpClient() {
        HttpClients.custom().build();
        if ("dev".equalsIgnoreCase(activeProfile)) {
            return HttpClients.custom()
                    .setDefaultRequestConfig(getRequestConfig())
                    .build();
        } else {
            return HttpClients.custom().build();
        }
    }

    public RequestConfig getRequestConfig() {
        RequestConfig config;
        if ("dev".equalsIgnoreCase(activeProfile)) {
            HttpHost proxy = new HttpHost("127.0.0.1", 7897, "http");
             config = RequestConfig.custom()
                    .setProxy(proxy)
                    .setConnectTimeout(30000)     // 连接超时 30 秒
                    .setSocketTimeout(60000)      // 读取超时 60 秒（关键！通过代理时必须调大）
                    .setConnectionRequestTimeout(30000)
                    .build();
        } else {
            config = RequestConfig.custom()
                    .setConnectTimeout(10000)
                    .setSocketTimeout(10000)
                    .setConnectionRequestTimeout(10000)
                    .build();
        }
        return config;
    }


    private String generateState() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }


}
