package com.munjie.omni.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {
 
    public static String doGet(String url) throws Exception {
        return doGet(url, null);
    }
 
    public static String doGet(String url, String token) throws Exception {
        HttpURLConnection con = createConnection(url, "GET", token);
        return handleResponse(con);
    }
 
    public static String doPost(String url, String params) throws IOException {
        return doPost(url, null, params);
    }
 
    public static String doPost(String url, String token, String params) throws IOException {
        HttpURLConnection con = createConnection(url, "POST", token);
        con.setDoOutput(true);
 
        // 发送 POST 请求
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = params.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
 
        return handleResponse(con);
    }
 
    public static String doPut(String url, String params) throws IOException {
        return doPut(url, null, params);
    }
 
    public static String doPut(String url, String token, String params) throws IOException {
        HttpURLConnection con = createConnection(url, "PUT", token);
        con.setDoOutput(true);
 
        // 发送 PUT 请求
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = params.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
 
        return handleResponse(con);
    }
 
    public static String doDelete(String url) throws IOException {
        return doDelete(url, null);
    }
 
    public static String doDelete(String url, String token) throws IOException {
        HttpURLConnection con = createConnection(url, "DELETE", token);
        return handleResponse(con);
    }
 
    private static HttpURLConnection createConnection(String url, String method, String token) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        if (token != null) {
            con.setRequestProperty("Authorization",token);
        }
        return con;
    }
 
    private static String handleResponse(HttpURLConnection con) throws IOException {
        int responseCode = con.getResponseCode();
        StringBuilder response = new StringBuilder();
 
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line.trim());
                }
            }
            return response.toString();
        } else {
            // 读取错误信息
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(con.getErrorStream()))) {
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                System.out.println("接口调用失败：" + errorResponse.toString());
            }
            return null;
        }
    }
}