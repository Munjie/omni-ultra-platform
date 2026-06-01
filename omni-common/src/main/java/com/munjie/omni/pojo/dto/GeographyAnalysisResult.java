package com.munjie.omni.pojo.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class GeographyAnalysisResult {
    private String overall;   // 总体掌握情况
    private String target;    // 目标
    private String measures;  // 达标措施
}