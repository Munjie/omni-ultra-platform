package com.munjie.omni.service;


import com.munjie.omni.pojo.entity.ScoreEntity;
import com.munjie.omni.pojo.vo.BoxPlotDataVO;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScoreAnalysisService {


    public List<BoxPlotDataVO> analyzeScoresForBoxPlot(List<ScoreEntity> studentList, String scoreType) {
        // 1. 根据班级分组并提取指定成绩
        Map<String, List<Double>> scoresByClass = studentList.stream()
            .collect(Collectors.groupingBy(
                    ScoreEntity::getLesson,
                Collectors.mapping(
                    s -> "geographyScore".equalsIgnoreCase(scoreType) ? s.getGeographyScore() : s.getTotalScore(),
                    Collectors.toList()
                )
            ));

        // 2. 对每个班级的成绩进行统计计算
        return scoresByClass.entrySet().stream()
            .map(entry -> calculateBoxPlotData(entry.getKey(), entry.getValue()))
            .filter(Objects::nonNull) // 过滤掉计算失败的班级
            .collect(Collectors.toList());
    }

    /**
     * 计算单个班级的五数概括和异常值
     */
    private BoxPlotDataVO calculateBoxPlotData(String className, List<Double> scores) {
        if (scores == null || scores.size() < 4) { // 至少需要4个数据点才能合理计算四分位数
            return null;
        }
        // 排序并转换为 double[]
        Collections.sort(scores);
        double[] data = scores.stream().mapToDouble(Double::doubleValue).toArray();

        Percentile percentile = new Percentile();
        double q1 = percentile.evaluate(data, 25);
        double median = percentile.evaluate(data, 50); // Q2
        double q3 = percentile.evaluate(data, 75);
        // 计算 IQR (四分位距)
        double iqr = q3 - q1;
        // 计算箱须的上下限 (非异常值的边界)
        double lowerBound = q1 - 1.5 * iqr;
        double upperBound = q3 + 1.5 * iqr;

        double minWhisker = scores.get(0);
        double maxWhisker = scores.get(scores.size() - 1);
        List<Double> outliers = new ArrayList<>();

        for (double score : scores) {
            if (score < lowerBound || score > upperBound) {
                outliers.add(score);
            }
        }

        for (double score : scores) {
            if (score >= lowerBound && score <= q1) {
                minWhisker = score;
            }
            if (score <= upperBound && score >= q3) {
                maxWhisker = score;
            }
        }

        BoxPlotDataVO result = new BoxPlotDataVO();
        result.setClassName(className);
        result.setQ1(q1);
        result.setMedian(median);
        result.setQ3(q3);
        result.setMin(minWhisker);
        result.setMax(maxWhisker);
        result.setOutliers(outliers);

        return result;
    }
}