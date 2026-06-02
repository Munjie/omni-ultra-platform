package com.munjie.omni.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.munjie.omni.pojo.dto.ExportProgress;
import com.munjie.omni.pojo.dto.ExportRequestDTO;
import com.munjie.omni.pojo.dto.GeographyAnalysisResult;
import com.munjie.omni.pojo.entity.QuestionAnalysisEntity;
import com.munjie.omni.pojo.entity.ScoreData;
import com.munjie.omni.pojo.entity.ScoreEntity;
import com.munjie.omni.service.GeographyAnalysisService;
import com.munjie.omni.service.QuestionAnalysisService;
import com.munjie.omni.service.ScoreService;
import com.munjie.omni.utils.PoiUtils;
import com.munjie.omni.utils.ScoreStatistics;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExportService {


    @Resource
    private ScoreService scoreService;

    @Resource
    private QuestionAnalysisService questionAnalysisService;


    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ScoreStatistics scoreStatistics;

    @Resource
    private GeographyAnalysisService geographyAnalysisService;

    private static Sheet sheet;


    @Async("excelExecutor")
    public void asyncExportReport(ExportRequestDTO requestDTO, String exportJobId) {
        Integer taskId = requestDTO.getTaskId();
        String title = requestDTO.getTitle();
        LambdaQueryWrapper<ScoreEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ObjectUtil.isNotNull(taskId), ScoreEntity::getTaskId, taskId);
        List<ScoreEntity> allScore = scoreService.list(wrapper);
        //分析查询
        LambdaQueryWrapper<QuestionAnalysisEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ObjectUtil.isNotNull(taskId), QuestionAnalysisEntity::getTaskId, taskId);
        List<QuestionAnalysisEntity> allQuestion = questionAnalysisService.list(lambdaQueryWrapper);
        // 1. 按照班级分组 (保持你原有的逻辑)
        Map<String, List<ScoreEntity>> classMap = allScore.stream()
                .collect(Collectors.groupingBy(
                        ScoreEntity::getLesson,
                        TreeMap::new,
                        Collectors.toList()
                ));
        int totalClasses = classMap.size();
        int weightPrep = 2;
        int weightAi = 10;
        int weightExcel = 2;
        int weightClassTotal = weightPrep + weightAi + weightExcel;
        int weightFinalSave = 5;
        int totalTicks = (weightClassTotal * totalClasses) + weightFinalSave;
        int currentTicks = 0;
        // 使用 SXSSFWorkbook 提升大文件写入性能
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            for (Map.Entry<String, List<ScoreEntity>> stringListEntry : classMap.entrySet()) {
                String key = stringListEntry.getKey();
                List<ScoreEntity> list = stringListEntry.getValue();
                String className = list.get(0).getLesson();
                currentTicks += weightPrep;
                updateProgress(exportJobId, (currentTicks * 100) / totalTicks, "processing", null, "计算 [" + className + "] 的统计指标中...");
                List<QuestionAnalysisEntity> entityList = allQuestion.stream().filter(m -> key.contentEquals(m.getClassName())).toList();
                // 创建工作簿
                sheet = workbook.createSheet(className + "质量分析报表");
                // 设置字体样式
                Font liShuFont = createFont("华文隶书", (short) 16, true);
                Font heiTiFont = createFont("黑体", (short) 16, true);
                Font weiWeiFont = createFont("宋体", (short) 11, false);
                Font songTiFont = createFont("宋体", (short) 11, true);
                Font boldFont = createFont(null, null, true);
                Font kaiTiFont = createFont("楷体_GB2312", (short) 12, true);
                Font xinweiFont = createFont("华文新魏", (short) 15, false);
                // 设置单元格样式
                CellStyle liShuStyle = createCellStyle(workbook, liShuFont);
                CellStyle heiTiStyle = createCellStyle(workbook, heiTiFont);
                CellStyle weiWeiStyle = createCellStyle(workbook, weiWeiFont);
                CellStyle songTiBoldStyle = createCellStyle(workbook, songTiFont);
                CellStyle boldStyle = createCellStyle(workbook, boldFont);
                CellStyle kaiTiStyle = createCellStyle(workbook, kaiTiFont);
                CellStyle xinStyle = createCellStyle(workbook, xinweiFont);
                // 边框
                weiWeiStyle.setBorderTop(BorderStyle.THIN);
                weiWeiStyle.setBorderBottom(BorderStyle.THIN);
                weiWeiStyle.setBorderLeft(BorderStyle.THIN);
                weiWeiStyle.setBorderRight(BorderStyle.THIN);
                // 边框
                songTiBoldStyle.setBorderTop(BorderStyle.THIN);
                songTiBoldStyle.setBorderBottom(BorderStyle.THIN);
                songTiBoldStyle.setBorderLeft(BorderStyle.THIN);
                songTiBoldStyle.setBorderRight(BorderStyle.THIN);
                Map<String, Object> map = scoreStatistics.calcScore(list, entityList);
                currentTicks += weightAi;
                updateProgress(exportJobId, (currentTicks * 100) / totalTicks, "processing", null, "[" + className + "] 成绩分析中...");
                // 调用大模型 (同步阻塞)
                GeographyAnalysisResult result = geographyAnalysisService.generateAnalysis(5, map).block();
                log.info("geographyAnalysisResultMono={}", result);
                if (ObjectUtil.isNotNull(result)) {
                    map.put("overall", result.getOverall());
                    map.put("target", result.getTarget());
                    map.put("measures", result.getMeasures());
                }
                String lightPoint = map.getOrDefault("high","").toString();
                String badPoint = map.getOrDefault("low","").toString();
                String lowScoreStudents = map.getOrDefault("lowScoreStudents","").toString();
                // 填充数据
                currentTicks += weightExcel;
                updateProgress(exportJobId, (currentTicks * 100) / totalTicks, "processing", null, "[" + className + "] 渲染中...");
                fillHeader(title, xinStyle, liShuStyle, heiTiStyle, weiWeiStyle, songTiBoldStyle, boldStyle, map, lightPoint, badPoint,lowScoreStudents);
            }
            File excelFile = PoiUtils.createExcelFile(workbook, title);
            currentTicks += weightFinalSave;
            int finalPercent = (currentTicks * 100) / totalTicks;
            updateProgress(exportJobId, finalPercent, "completed", excelFile.getAbsolutePath(), "成绩质量分析完成");
        } catch (Exception e) {
            log.error("异步导出异常", e);
            updateProgress(exportJobId, 0, "failed",null, e.getMessage());
        } finally {
            // 释放业务锁
            redisTemplate.delete("export_lock:" + taskId);
        }
    }



    // 辅助更新方法封装
    private void updateProgress(String id, int percent, String status, String fileId, String stageText) {
        redisTemplate.opsForValue().set("export_progress:" + id,
                new ExportProgress(percent, status, fileId, stageText, null), Duration.ofHours(1));
    }

    private static Font createFont(String fontName, Short fontSize, Boolean bold) {
        Workbook workbook = sheet.getWorkbook();
        Font font = workbook.createFont();
        if (fontName != null) {
            font.setFontName(fontName);
        }
        if (fontSize != null) {
            font.setFontHeightInPoints(fontSize);
        }
        if (bold != null) {
            font.setBold(bold);
        }
        return font;
    }

    private static CellStyle createCellStyle(Workbook workbook, Font font) {
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static void fillHeader(String title,CellStyle xinStyle, CellStyle liShuStyle, CellStyle heiTiStyle, CellStyle weiWeiStyle, CellStyle songTiBoldStyle, CellStyle kaiTiStyle, Map<String, Object> map, String lightPoint,String badPoint, String lowScoreStudents) {

        Row row1 = sheet.createRow(0);
        mergeAndSetCell(sheet, row1, 0, 9, "南京一中明发滨江分校", liShuStyle);

        Row row2 = sheet.createRow(1);
        mergeAndSetCell(sheet,row2, 0, 9, title, heiTiStyle);

        // 第3行
        Row row3 = sheet.createRow(2);
        fillRowName(row3, new String[]{"学科", "地理", "班级", map.get("lesson").toString(), "应考人数", map.get("shouldTotal").toString(), "实考人数", map.get("relTotal").toString(), "任课教师", "陈赛赛"}, kaiTiStyle);

        // 第4行
        Row row4 = sheet.createRow(3);
        fillRowName(row4, new String[]{"班级总分", map.get("totalScore").toString(), "均分", map.get("averageScore").toString(), "最高分", map.get("maxScore").toString(), "最低分", map.get("minScore").toString(), "极差", map.get("range").toString()}, kaiTiStyle);

        // 第5行
        Row row5 = sheet.createRow(4);
        mergeAndSetCell(sheet,row5, 0, 9, "全班学生考试成绩总表", xinStyle);

        // 第6行
        Row row6 = sheet.createRow(5);
        fillRowName(row6, new String[]{"分数段", "1-9", "10-19", "20-29", "30-39", "40-50", "", "", "", "",}, songTiBoldStyle);

        // 第7行
        Row row7 = sheet.createRow(6);
        fillRowName(row7, new String[]{"人数", map.get("one").toString(), map.get("ten").toString(), map.get("twenty").toString(), map.get("thirty").toString(), map.get("forty").toString(), "", "", "", "",}, songTiBoldStyle);
        // setCellFontStyle(row7.createCell(0), "人数", songTiBoldStyle);

        // 第8行
        Row row8 = sheet.createRow(7);
        fillRowName(row8, new String[]{"及格人数", map.get("passCount").toString(), "及格率", map.get("passRate").toString(), "优分人数", map.get("excellentCount").toString(), "优分率", map.get("excellentRate").toString(), "低分率", map.get("failRate").toString()}, songTiBoldStyle);

        // 第9行空行
        sheet.createRow(8);

        // 第10行
        Row row10 = sheet.createRow(9);
        mergeAndSetCell(sheet,row10, 0, 1, "一、各大题得分", checkLeft(songTiBoldStyle));


        // 第11行
        Row row11 = sheet.createRow(10);
        fillRowName(row11, new String[]{"题号", "1-10", "", "11-30", "", "31-40", "", "", "", ""}, weiWeiStyle);

        // 第12行
        Row row12 = sheet.createRow(11);
        fillRowName(row12, new String[]{"得分率%", ObjectUtil.isNotNull(map.get("oneRate")) ? map.get("oneRate").toString() : "", "", ObjectUtil.isNotNull(map.get("twentyRate")) ? map.get("twentyRate").toString() : "", "", ObjectUtil.isNotNull(map.get("thirtyRate")) ? map.get("thirtyRate").toString() : "", "", "", "", ""}, weiWeiStyle);

        // 第13行
        Row row13 = sheet.createRow(12);
        mergeAndSetCell(sheet,row13, 0, 1, "二、质量分析", checkLeft(songTiBoldStyle));

        // 第14行
        Row row14 = sheet.createRow(13);
        mergeAndSetCell(sheet,row14, 0, 9, "1、学生知识点掌握情况", checkLeft(songTiBoldStyle));

        Row row15 = sheet.createRow(14);
        mergeCells(sheet,row15, 14, 17, 0, 9, "总体:" + map.getOrDefault("overall",""), checkLeftAndTop(songTiBoldStyle));

        Row row18 = sheet.createRow(18);
        mergeCells(sheet,row18, 18, 21, 0, 9, "闪光点:" + lightPoint, checkLeftAndTop(songTiBoldStyle));

        Row row19 = sheet.createRow(22);
        mergeCells(sheet,row19, 22, 25, 0, 9, "薄弱点:" + badPoint, checkLeftAndTop(songTiBoldStyle));

        Row row20 = sheet.createRow(26);
        mergeAndSetCell(sheet,row20, 0, 9, "2、薄弱人群", checkLeft(songTiBoldStyle));

        Row row21 = sheet.createRow(27);
        mergeCells(sheet,row21, 27, 30, 0, 9, "重点转化人头： " + lowScoreStudents, checkLeftAndTop(songTiBoldStyle));

        Row row22 = sheet.createRow(31);
        mergeAndSetCell(sheet,row22, 0, 9, "3、目标与措施", checkLeft(songTiBoldStyle));

        Row row23 = sheet.createRow(32);
        mergeCells(sheet,row23, 32, 35, 0, 9, "目标：" + map.getOrDefault("target",""), checkLeftAndTop(songTiBoldStyle));

        Row row24 = sheet.createRow(36);
        mergeCells(sheet,row24, 36, 40, 0, 9, "达标措施："+ map.getOrDefault("measures",""), checkLeftAndTop(songTiBoldStyle));
    }



    private static CellStyle checkLeft(CellStyle cellStyle) {
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return cellStyle;

    }

    private static CellStyle checkLeftAndTop(CellStyle cellStyle) {
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        // cellStyle.setFont( createFont("宋体", (short) 11, false));
        return cellStyle;
    }

    private static void mergeAndSetCell(Sheet sheet, Row row, int startCol, int endCol, String value, CellStyle style) {
        CellRangeAddress region = new CellRangeAddress(row.getRowNum(), row.getRowNum(), startCol, endCol);
        sheet.addMergedRegion(region);
        Cell cell = row.createCell(startCol);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        RegionUtil.setBorderTop(style.getBorderTop(), region, sheet);
        RegionUtil.setBorderBottom(style.getBorderBottom(), region, sheet);
        RegionUtil.setBorderLeft(style.getBorderLeft(), region, sheet);
        RegionUtil.setBorderRight(style.getBorderRight(), region, sheet);
    }





    /**
     * 合并单元格并设置值、样式及完整的边框
     */
    private static void mergeCells(Sheet sheet, Row row, int startRow, int endRow, int startCol, int endCol, String defaultValue, CellStyle style) {
        style.setWrapText(true);
        CellRangeAddress region = new CellRangeAddress(startRow, endRow, startCol, endCol);
        sheet.addMergedRegion(region);
        Cell cell = row.createCell(startCol);
        cell.setCellValue(defaultValue);
        cell.setCellStyle(style);
        RegionUtil.setBorderTop(style.getBorderTop(), region, sheet);
        RegionUtil.setBorderBottom(style.getBorderBottom(), region, sheet);
        RegionUtil.setBorderLeft(style.getBorderLeft(), region, sheet);
        RegionUtil.setBorderRight(style.getBorderRight(), region, sheet);

    }


    private static void fillRowName(Row row, String[] values, CellStyle style) {
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(values[i]);
            cell.setCellStyle(style);
        }
    }

    private static void setCellFontStyle(Cell cell, String value, CellStyle style) {
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static void fillData(List<ScoreData> scoreDataList, CellStyle boldStyle) {
        int rowIndex = 3; // 从第15行开始填充数据
        for (ScoreData scoreData : scoreDataList) {
            Row row = sheet.createRow(rowIndex++);
            // 填充学科
            setCellFontStyle(row.createCell(0), scoreData.getSubject(), boldStyle);
            // 填充班级
            setCellFontStyle(row.createCell(1), scoreData.getClassroom(), boldStyle);
            // 填充应考人数
            setCellValueAndStyle(row.createCell(2), scoreData.getExamCount(), boldStyle);
            // 填充实考人数
            setCellValueAndStyle(row.createCell(3), scoreData.getActualExamCount(), boldStyle);
            // 填充任课教师
            setCellFontStyle(row.createCell(4), scoreData.getTeacher(), boldStyle);


        }
    }

    private static void setCellValueAndStyle(Cell cell, String value, CellStyle style) {
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static void setCellValueAndStyle(Cell cell, int value, CellStyle style) {
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

}
