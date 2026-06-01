package com.munjie.omni.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.munjie.omni.pojo.entity.TotalScoreEntity;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TotalScoreExportService {




    public void exportScoreTip( List<TotalScoreEntity> studentScores) {
        String fileName = "score_sort.xlsx";

        // Define header and content styles
        WriteCellStyle headerStyle = new WriteCellStyle();
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);


        WriteCellStyle contentStyle = new WriteCellStyle();
        contentStyle.setBorderTop(BorderStyle.THIN);
        contentStyle.setBorderBottom(BorderStyle.THIN);
        contentStyle.setBorderLeft(BorderStyle.THIN);
        contentStyle.setBorderRight(BorderStyle.THIN);
        contentStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);

        HorizontalCellStyleStrategy styleStrategy = new HorizontalCellStyleStrategy(headerStyle, contentStyle);

        // Collect all rows to write at once
        List<List<String>> allData = new ArrayList<>();

        for (TotalScoreEntity score : studentScores) {
            allData.add(createHeaders());
            allData.add(createRow(score));
            // Add blank lines for separation
            for (int i = 0; i < 2; i++) {
                allData.add(new ArrayList<>());
            }
        }

        // Write all data to Excel with custom cell style
        EasyExcel.write(fileName)
                .registerWriteHandler(styleStrategy)
                .sheet("Student Scores")
                .doWrite(allData);

    }

    private static List<String> createHeaders() {
        List<String> headers = Arrays.asList("姓名", "语文", "数学", "英语", "物理","化学", "政治", "历史", "总分","排名");
        return new ArrayList<>(headers);
    }

    private static List<String> createRow(TotalScoreEntity score) {
        return Arrays.asList(
                score.getStudentName(),
                String.valueOf(score.getChinese()),
                String.valueOf(score.getMath()),
                String.valueOf(score.getEnglish()),
                String.valueOf(score.getPhysics()),
                String.valueOf(score.getChemistry()),
                String.valueOf(score.getPolitics()),
                String.valueOf(score.getHistory()),
             /*   String.valueOf(score.getGeography()),
                String.valueOf(score.getBiology()),*/
                String.valueOf(score.getTotalScore()),
                String.valueOf(score.getRanking())
        );
    }


}