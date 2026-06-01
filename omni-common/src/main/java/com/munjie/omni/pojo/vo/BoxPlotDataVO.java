package com.munjie.omni.pojo.vo;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BoxPlotDataVO {
    private String className; // 班级名称
    private Double min;       // 最小值
    private Double q1;        // 下四分位数 (25%)
    private Double median;    // 中位数 (50%)
    private Double q3;        // 上四分位数 (75%)
    private Double max;       // 最大值
    private List<Double> outliers; // 异常值


}