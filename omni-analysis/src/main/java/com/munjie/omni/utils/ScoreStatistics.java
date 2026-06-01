package com.munjie.omni.utils;

import cn.hutool.core.collection.CollUtil;
import com.munjie.omni.pojo.entity.QuestionAnalysisEntity;
import com.munjie.omni.pojo.entity.ScoreEntity;
import com.munjie.omni.service.GeographyAnalysisService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author muwen
 */
@Slf4j
@Service
public class ScoreStatistics {

    @Resource
    private GeographyAnalysisService geographyAnalysisService;

    public Map<String,Object> calcScore(List<ScoreEntity> scores, List<QuestionAnalysisEntity> analysisEntities) {
        Map<String,Object> map = new HashMap<>(16);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        Double maxScore = getMaxScore(scores);
        Double minScore = getMinScore(scores);
        double averageScore = getAverageScore(scores);
        Double totalScore = getTotalScore(scores);
        Double range = getRange(scores);
        long passCount = getPassCount(scores);
        double passRate = getPassRate(scores);
        long excellentCount = getExcellentCount(scores);
        double excellentRate = getExcellentRate(scores);
        double failRate = getFailRate(scores);
        map.put("maxScore" ,  decimalFormat.format(maxScore));
        map.put("minScore" ,  decimalFormat.format(minScore));
        map.put("averageScore" ,   decimalFormat.format(averageScore));
        map.put("totalScore" ,   decimalFormat.format(totalScore));
        map.put("range" ,   decimalFormat.format(range));
        map.put("passCount" ,   decimalFormat.format(passCount));
        map.put("passRate" ,   decimalFormat.format(passRate * 100) +  "%");
        map.put("excellentCount" ,   decimalFormat.format(excellentCount));
        map.put("excellentRate" ,   (decimalFormat.format(excellentRate * 100))  +  "%");
        map.put("failRate" ,   decimalFormat.format(failRate * 100) +  "%");
        map.put("lesson" ,   scores.get(0).getLesson());
        map.put("shouldTotal",scores.size());
        map.put("relTotal",scores.size());

        long one = getScoreCountInRange(scores, score -> score >= 1 && score <= 9);
        long ten = getScoreCountInRange(scores, score -> score >= 10 && score <= 19);
        long twenty = getScoreCountInRange(scores, score -> score >= 20 && score <= 29);
        long thirty = getScoreCountInRange(scores, score -> score >= 30 && score <= 39);
        long forty = getScoreCountInRange(scores, score -> score >= 40 && score <= 50);
        map.put("one", one);
        map.put("ten", ten);
        map.put("twenty", twenty);
        map.put("thirty", thirty);
        map.put("forty", forty);
        //得分率
        BigDecimal oneRate = calculateAverageScoreRate(analysisEntities, 1, 10);
        BigDecimal twentyRate = calculateAverageScoreRate(analysisEntities, 11, 30);
        BigDecimal thirtyRate = calculateAverageScoreRate(analysisEntities, 31, 40);
        map.put("oneRate", oneRate);
        map.put("twentyRate", twentyRate);
        map.put("thirtyRate", thirtyRate);

        Map<String, String> stringStringMap = processQuestionAnalysis(analysisEntities);
        map.putAll(stringStringMap);
        String lowScoreStudents = getLowScoreStudents(scores);
        map.put("lowScoreStudents", lowScoreStudents);

        String lightPoint = map.getOrDefault("high","").toString();
        String badPoint = map.getOrDefault("low","").toString();

        // Print statistics
        System.out.println("最高分: " + maxScore);
        System.out.println("最低分: " + minScore);
        System.out.println("均分: " + averageScore);
        System.out.println("总分: " + totalScore);
        System.out.println("极差: " + range);
        System.out.println("及格人数: " + passCount);
        System.out.println("及格率: " + passRate);
        System.out.println("优分人数: " + excellentCount);
        System.out.println("优分率: " + excellentRate);
        System.out.println("低分率: " + failRate);
        System.out.println("1至9分数段人数: " + one);
        System.out.println("10至19分数段人数: " + ten);
        System.out.println("20至29分数段人数: " + twenty);
        System.out.println("30至39分数段人数: " + thirty);
        System.out.println("40至50分数段人数: " + forty);
        System.out.println("1至10题得分率: " + oneRate);
        System.out.println("11至30分数段人数: " + twentyRate);
        System.out.println("31至40分数段人数: " + thirtyRate);
        System.out.println("闪光点(得分率大于85的题号): " + lightPoint);
        System.out.println("薄弱点点(得分率小于70的题号): " + badPoint);
        System.out.println("重点转化人头(分数小于30分的学生): " + lowScoreStudents);

//        GeographyAnalysisResult result = geographyAnalysisService.generateAnalysis(5, map).block();
//       log.info("geographyAnalysisResultMono={}", result);
//        if (ObjectUtil.isNotNull(result)) {
//            map.put("overall", result.getOverall());
//            map.put("target", result.getTarget());
//            map.put("measures", result.getMeasures());
//        }
//        map.forEach((string, o) -> System.out.println("o.toString() = " + o.toString()));
        return map;
    }


    /**
     * 计算指定区间的平均得分率
     *
     * @param dataList 数据列表
     * @param start 起始题号
     * @param end 结束题号
     * @return 平均得分率，如果没有数据，则返回-1
     */
    private static BigDecimal calculateAverageScoreRate(List<QuestionAnalysisEntity> dataList, int start, int end) {
        if (CollUtil.isEmpty(dataList)) {
            return null;
        }
        OptionalDouble avgScoreRate = dataList.stream()
                .filter(p -> p.getSubQuestionNumber() >= start && p.getSubQuestionNumber() <= end)
                .mapToDouble(QuestionAnalysisEntity::getScoreRate)
                .average();
        return avgScoreRate.isPresent() ? new BigDecimal(avgScoreRate.getAsDouble()).setScale(2,BigDecimal.ROUND_HALF_UP): null;
    }



    private static long getScoreCountInRange(List<ScoreEntity> scores, Predicate<Double> condition) {
        return scores.stream()
                .map(ScoreEntity::getGeographyScore)
                .filter(condition)
                .count();
    }

    private static Double getMaxScore(List<ScoreEntity> scores) {
        return scores.stream()
                .mapToDouble(ScoreEntity::getGeographyScore)
                .max()
                .orElse(0);
    }

    private static Double getMinScore(List<ScoreEntity> scores) {
        return scores.stream()
                .mapToDouble(ScoreEntity::getGeographyScore)
                .min()
                .orElse(0);
    }

    private static Double getAverageScore(List<ScoreEntity> scores) {
        return scores.stream()
                .mapToDouble(ScoreEntity::getGeographyScore)
                .average()
                .orElse(0.0);
    }

    private static Double getTotalScore(List<ScoreEntity> scores) {
        return scores.stream()
                .mapToDouble(ScoreEntity::getGeographyScore)
                .sum();
    }

    private static Double getRange(List<ScoreEntity> scores) {
        Double max = getMaxScore(scores);
        Double min = getMinScore(scores);
        return max - min;
    }

    private static long getPassCount(List<ScoreEntity> scores) {
        return scores.stream()
                .filter(score -> score.getGeographyScore() >= 30)
                .count();
    }

    private static double getPassRate(List<ScoreEntity> scores) {
        long passCount = getPassCount(scores);
        return (double) passCount / scores.size();
    }

    private static long getExcellentCount(List<ScoreEntity> scores) {
        return scores.stream()
                .filter(score -> score.getGeographyScore() >= 45)
                .count();
    }

    private static double getExcellentRate(List<ScoreEntity> scores) {
        long excellentCount = getExcellentCount(scores);
        return (double) excellentCount / scores.size();
    }

    private static double getFailRate(List<ScoreEntity> scores) {
        long failCount = scores.stream()
                .filter(score -> score.getGeographyScore() < 20)
                .count();
        return (double) failCount / scores.size();
    }


    // 闪光点> 得分率85/薄弱点 < 得分率70
    public static Map<String,String> processQuestionAnalysis(List<QuestionAnalysisEntity> entities) {
        if (CollUtil.isEmpty(entities)) {
            return HashMap.newHashMap(16);
        }
        Map<String, List<String>> highScoresByClass = new HashMap<>();
        Map<String, List<String>> lowScoresByClass = new HashMap<>();
        Pattern numberPattern = Pattern.compile("\\d+");
        for (QuestionAnalysisEntity entity : entities) {
            String className = entity.getClassName();
            Double scoreRate = entity.getScoreRate();
            String questionNumber = entity.getQuestionNumber();
            Matcher matcher = numberPattern.matcher(questionNumber);
            String numberOnly = matcher.find() ? matcher.group() : "";
            String formattedResult = numberOnly + "(" + scoreRate + ")";
            highScoresByClass.computeIfAbsent(className, k -> new ArrayList<>());
            lowScoresByClass.computeIfAbsent(className, k -> new ArrayList<>());
            if (scoreRate > 85) {
                highScoresByClass.get(className).add(formattedResult);
            } else if (scoreRate < 70) {
                lowScoresByClass.get(className).add(formattedResult);
            }
        }
        List<String> classes = new ArrayList<>(highScoresByClass.keySet());
        classes.addAll(lowScoresByClass.keySet());
        classes = classes.stream().distinct().sorted().collect(Collectors.toList());
        StringBuilder high = new StringBuilder();
        StringBuilder low = new StringBuilder();
        for (String className : classes) {
            // 大于85的结果
            List<String> highScores = highScoresByClass.getOrDefault(className, new ArrayList<>());
            if (highScores.isEmpty()) {
                high.append("无");
            } else {
                high.append(String.join(",", highScores));
            }
            // 小于70的结果
            List<String> lowScores = lowScoresByClass.getOrDefault(className, new ArrayList<>());
            if (lowScores.isEmpty()) {
                low.append("无");
            } else {
                low.append(String.join(",", lowScores));
            }
        }
        Map<String,String> result = new HashMap<>();
        result.put("high",high.toString());
        result.put("low",low.toString());
        return result;
    }


    // 筛选分数低于30分的同学
    public static String getLowScoreStudents(List<ScoreEntity> list) {
        if (list == null || list.isEmpty()) {
            return "暂无";
        }
        return list.stream()
                .filter(s -> s.getName() != null && s.getGeographyScore() != null && s.getGeographyScore() < 30)
                .map(s -> String.format("%s(%d)", s.getName(), s.getGeographyScore().intValue()))
                .collect(Collectors.joining("，"));
    }




}
