package com.munjie.omni.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.munjie.omni.pojo.dto.GeographyAnalysisResult;
import com.munjie.omni.pojo.entity.AiModelInfoEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeographyAnalysisService {

    @Resource
    private AiModelInfoService service;


    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeographyAnalysisService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * 生成地理成绩分析报告
     */
    public Mono<GeographyAnalysisResult> generateAnalysis(Integer id, Map<String, Object> examData) {
        AiModelInfoEntity info = service.getById(id);

        // 1. 构建包含考试数据的 Prompt
        String prompt = buildPrompt(examData);
        System.out.println("prompt = " + prompt);
        // 2. 构造阿里千问/OpenAI标准的请求体
        Map<String, Object> request = new HashMap<>();
        request.put("model", info.getModelValue());
        request.put(
                "messages", List.of(
                        Map.of("role", "assistant", "content", "你好！我是 AI 助手，今天想聊点什么？"),
                Map.of("role", "user", "content", prompt)
        )
        );
        request.put("response_format", Map.of("type", "json_object"));
        request.put("temperature", 0.3); // 降低温度，让模型输出更理性的分析，减少幻觉

        // 3. 发送请求
        Mono<GeographyAnalysisResult> authorization = webClientBuilder.build()
                .post()
                .uri(info.getBaseUrl())
                .header("Authorization", "Bearer " + info.getApiKey())
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class).flatMap(body ->
                                Mono.error(new RuntimeException("API调用错误: " + body))
                        )
                )
                .bodyToMono(String.class)
                .doOnNext(rawBody -> log.info("========== 大模型原生返回内容: \n{} \n==========", rawBody))
                .timeout(Duration.ofSeconds(45))
                .map(this::parseJsonToResult)
                .doOnError(e -> log.error("执行过程中发生异常: ", e));
        System.out.println("authorization = " + authorization);
        return authorization;
    }

    /**
     * 将收集到的变量替换到 Prompt 模板中
     */
    private String buildPrompt(Map<String, Object> data) {
        return String.format("""
                        你现在是一名资深的初中地理教师兼教学教研专家。请根据以下某次地理考试的成绩真实统计数据，为我生成一份专业的“成绩质量分析报告”。
                        【考试统计数据】
                        最高分：%s，最低分：%s，均分：%s，总分：%s，极差：%s
                        及格人数：%s，及格率：%s
                        优分人数：%s，优分率：%s，低分率：%s
                        
                        【分数段分布情况】
                         - 40至50分(高分段): %s 人
                         - 30至39分(中分段): %s 人
                         - 20至29分(低分段): %s 人
                         - 1至19分(极低分段): %s 人
                        闪光点(得分率>85%%的题号)：%s
                        薄弱点(得分率<70%%的题号)：%s
                        重点转化人头(<30分的学生)：%s
                        
                        【任务要求】
                        请结合上述数据，提炼出成绩质量分析表所需的3个核心模块。
                        你必须严格以JSON格式输出，不要包含任何Markdown标记，只输出纯JSON字符串。JSON结构如下：
                        {
                          "overall": "总体情况分析（结合考试统计数据,分数分布和薄弱点，评价知识掌握情况，50字左右）",
                          "target": "目标（设定下阶段可量化的及格率/优分率提升目标，50字左右）",
                          "measures": "达标措施（针对薄弱点和重点转化人头，给出3点具体干预措施，50字左右）"
                        }
                        """,
                data.get("maxScore"), data.get("minScore"), data.get("averageScore"),
                data.get("totalScore"), data.get("range"), data.get("passCount"),
                data.get("passRate"), data.get("excellentCount"), data.get("excellentRate"),
                data.get("failRate"),
                data.get("forty"),
                data.get("thirty"),
                data.get("twenty"),
                ( (Long)data.get("one") + (Long)data.get("ten") ),
                data.get("high"),
                data.get("low"),
                data.get("lowScoreStudents")
        );
    }

    /**
     * 解析大模型返回的 JSON，去除可能的 Markdown 干扰
     */
    private GeographyAnalysisResult parseJsonToResult(String responseBody) {
        try {
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(responseBody);
            String content = rootNode.path("choices").get(0).path("message").path("content").asText();
            content = content.replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();
            return objectMapper.readValue(content, GeographyAnalysisResult.class);
        } catch (Exception e) {
            throw new RuntimeException("解析大模型返回结果失败: " + responseBody, e);
        }
    }
}