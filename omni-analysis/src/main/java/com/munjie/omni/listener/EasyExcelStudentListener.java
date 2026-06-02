package com.munjie.omni.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.munjie.omni.pojo.entity.ScoreEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Slf4j
public class EasyExcelStudentListener extends AnalysisEventListener<ScoreEntity> {

    private List<ScoreEntity> dataList;

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        log.info(">>>> EasyExcel 实际解析到的表头 Map: {}", headMap);
        System.out.println();
    }

    @Override
    public void invoke(ScoreEntity data, AnalysisContext context) {
        System.out.println("data.getName() = " + data.getName());
        dataList.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
    }



}