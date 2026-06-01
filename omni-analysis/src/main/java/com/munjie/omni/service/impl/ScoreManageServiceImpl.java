package com.munjie.omni.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.munjie.omni.pojo.dto.HomeReqDTO;
import com.munjie.omni.pojo.entity.ScoreEntity;
import com.munjie.omni.pojo.vo.BoxPlotDataVO;
import com.munjie.omni.pojo.vo.ClassScore;
import com.munjie.omni.pojo.vo.ClassScorePie;
import com.munjie.omni.pojo.vo.HomeResVO;
import com.munjie.omni.service.ScoreAnalysisService;
import com.munjie.omni.service.ScoreManageService;
import com.munjie.omni.service.ScoreService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ScoreManageServiceImpl implements ScoreManageService {

    @Resource
    private ScoreService scoreService;

    @Resource
    private ScoreAnalysisService analysisService;


    @Override
    public HomeResVO homeData(HomeReqDTO homeReqDTO) {
        ClassScore scores = ClassScore.builder()
                .categories(Arrays.asList("18班", "19班", "20班", "21班"))
                .value(Arrays.asList(1450.3, 1552.6, 1587.0, 1468.0)).build();
        List<ClassScorePie> scorePies = Arrays.asList(
                new ClassScorePie("及格", 320),
                new ClassScorePie("不及格", 120)

        );
        String maxSumClass = StrUtil.EMPTY;
        Double maxSumScore = null;
        String maxAvgClass = StrUtil.EMPTY;
        Double maxAvgScore = null;
        String taskId = homeReqDTO.getTaskName();
        LambdaQueryWrapper<ScoreEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(taskId), ScoreEntity::getTaskId, taskId);
        List<ScoreEntity> list = scoreService.list(wrapper);
        List<BoxPlotDataVO> geographyScore = analysisService.analyzeScoresForBoxPlot(list, "geographyScore");


        Map<String, Double> geoSum = list.stream()
                .collect(Collectors.groupingBy(
                        ScoreEntity::getLesson,
                        Collectors.summingDouble(ScoreEntity::getGeographyScore)
                ));


        if (CollUtil.isNotEmpty(geoSum)) {
            maxSumClass = Collections.max(geoSum.entrySet(), Map.Entry.comparingByValue()).getKey();
            maxSumScore = geoSum.get(maxSumClass);
        }

        Map<String, Double> geoAvg = list.stream()
                .collect(Collectors.groupingBy(
                        ScoreEntity::getLesson,
                        Collectors.averagingDouble(ScoreEntity::getGeographyScore)
                ));
        if (CollUtil.isNotEmpty(geoAvg)) {
            maxAvgClass = Collections.max(geoAvg.entrySet(), Map.Entry.comparingByValue()).getKey();
            maxAvgScore = geoAvg.get(maxAvgClass);
        }


        // 输出
        System.out.println("地理成绩总分最高的班级: " + maxSumClass + "，总分 = " + maxSumScore);
        System.out.println("地理成绩平均分最高的班级: " + maxAvgClass + "，平均分 = " + maxAvgScore);

        // ① 地理 > 40 分人数最多的班级
        Map<String, Long> gt40Count = list.stream()
                .filter(s -> s.getGeographyScore() != null && s.getGeographyScore() > 40)
                .collect(Collectors.groupingBy(
                        ScoreEntity::getLesson,
                        Collectors.counting()
                ));

        String maxGt40Class = gt40Count.isEmpty()
                ? null
                : Collections.max(gt40Count.entrySet(), Map.Entry.comparingByValue()).getKey();


// ② 地理 < 30 分人数最多的班级
        Map<String, Long> lt30Count = list.stream()
                .filter(s -> s.getGeographyScore() != null && s.getGeographyScore() < 30)
                .collect(Collectors.groupingBy(
                        ScoreEntity::getLesson,
                        Collectors.counting()
                ));

        String maxLt30Class = lt30Count.isEmpty()
                ? null
                : Collections.max(lt30Count.entrySet(), Map.Entry.comparingByValue()).getKey();


// 输出
        System.out.println("40分以上人数最多的班级: " + maxGt40Class
                + "，人数 = " + gt40Count.getOrDefault(maxGt40Class, 0L));

        System.out.println("30以下人数最多的班级: " + maxLt30Class
                + "，人数 = " + lt30Count.getOrDefault(maxLt30Class, 0L));


        return HomeResVO.builder()
                .maxGtClass(maxGt40Class)
                .maxGt(gt40Count.getOrDefault(maxGt40Class, 0L))
                .maxLtClass(maxLt30Class)
                .maxLt(lt30Count.getOrDefault(maxLt30Class, 0L))
                .maxSumClass(maxSumClass)
                .maxAvgClass(maxAvgClass)
                .total(maxSumScore)
                .avg(maxAvgScore)
                .scoresBar(scores)
                .scorePies(scorePies)
                .boxPlotDataVOS(geographyScore)
                .build();
    }
}
