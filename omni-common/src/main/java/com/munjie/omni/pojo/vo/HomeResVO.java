package com.munjie.omni.pojo.vo;

import lombok.*;

import java.util.List;

/**
 * @Description: TODO
 * @author: Munjie
 * @date: 2024/10/27日 21:13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HomeResVO {
    private String maxGtClass;
    private String maxLtClass;
    private Long maxGt;
    private Long maxLt;
    private String maxSumClass;
    private String maxAvgClass;
    private Double  total;
    private Double  avg;
    private ClassScore scoresBar;
    private List<ClassScorePie> scorePies;
    private List<BoxPlotDataVO> boxPlotDataVOS;

}
