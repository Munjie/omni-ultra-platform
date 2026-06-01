package com.munjie.omni.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * EdgeFn AI 聊天接口工具类
 * 基于 JDK 11+ java.net.http.HttpClient 实现
 */
public class EdgeAiUtils {

    // API 地址
    private static final String API_URL = "https://api.edgefn.net/v1/chat/completions";
    // 指定模型名称
    private static final String MODEL_NAME = "DeepSeek-R1-0528-Qwen3-8B";

    /**
     * 发送聊天请求
     *
     * @param apiKey  你的 API Key
     * @param message 用户发送的消息内容
     * @return 服务器返回的 JSON 响应字符串
     */
    public static String sendChatRequest(String apiKey, String message) {
        // 1. 构建 HttpClient (设置超时时间)
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // 2. 构建请求体 (JSON 格式)
        // 注意：生产环境建议使用 Jackson 或 Gson 库来处理 JSON，避免手动拼接时的转义错误
        // 这里为了演示无依赖运行，使用了简单的字符串格式化，并对用户输入做了基础转义
        String safeMessage = escapeJsonString(message);
        
        String jsonBody = String.format(
                "{\n" +
                "  \"model\": \"%s\",\n" +
                "  \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}]\n" +
                "}", 
                MODEL_NAME, safeMessage
        );

        // 3. 构建 HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey) // 添加认证头
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // 4. 发送请求并获取响应
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // 检查状态码
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.err.println("请求失败，状态码: " + response.statusCode());
                return response.body(); // 返回错误详情
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 简单的 JSON 字符串转义工具
     * 防止用户输入中的引号、换行符破坏 JSON 结构
     */
    private static String escapeJsonString(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    // --- 测试 Main 方法 ---
    public static void main(String[] args) {
        // 请替换为你真实的 API Key
        String myApiKey = "sk-iByMOL9Ju4ldrXlTBc689aFfF5934331A53081A594856270";
        String userMessage = "你是谁？";

        System.out.println("正在发送请求...");
        
        String response = EdgeAiUtils.sendChatRequest(myApiKey, userMessage);
        
        System.out.println("--- 服务器响应 ---");
        System.out.println(response);
    }
}