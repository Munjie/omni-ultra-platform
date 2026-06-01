package com.munjie.omni.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVisitDto {
    private long todayPageViews;          // 今日浏览量（PV）
    private long todayUniqueVisitors;     // 今日独立访客（UV）
    private long totalPageViews;          // 总浏览量
    private long todayNewUsers;          // 今日新增用户
    private double pageViewsChangePercentage; // 较昨日浏览量变化 %

}