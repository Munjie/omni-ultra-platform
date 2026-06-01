package com.munjie.omni.listener;


import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.munjie.omni.pojo.entity.ScoreEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DynamicHeaderListener extends AnalysisEventListener<Map<Integer, String>> {

    private final List<ScoreEntity> list;
    private final Map<Integer, String> columnFullHeadNameMap = new HashMap<>();
    private final Map<String, Integer> fieldIndexMap = new HashMap<>();
    public DynamicHeaderListener(List<ScoreEntity> list) {
        this.list = list;
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        headMap.forEach((index, cellValue) -> {
            if (cellValue == null || cellValue.trim().isEmpty()) {
                return;
            }
            String fullName = columnFullHeadNameMap.getOrDefault(index, "");
            fullName += cellValue;
            columnFullHeadNameMap.put(index, fullName);
            String currentFull = columnFullHeadNameMap.get(index);
            if (currentFull.contains("学号")) {
                fieldIndexMap.put("studentNo", index);
            }
            if (currentFull.contains("姓名")) {
                fieldIndexMap.put("name", index);
            }
            if (currentFull.contains("行政班级")) {
                fieldIndexMap.put("lesson", index);
            }
            if (currentFull.contains("学校")) {
                fieldIndexMap.put("school", index);
            }
            if (currentFull.contains("地理") && currentFull.contains("分数")) {
                fieldIndexMap.put("geoScore", index);
            }
        });
    }

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        // 数据行处理
        ScoreEntity dto = new ScoreEntity();

        // 使用前面拼出来的索引取值
        if (fieldIndexMap.containsKey("studentNo")) {
            dto.setStudentId(data.get(fieldIndexMap.get("studentNo")));
        }
        if (fieldIndexMap.containsKey("name")) {
            dto.setName(data.get(fieldIndexMap.get("name")));
        }

        if (fieldIndexMap.containsKey("lesson")) {
            dto.setLesson(data.get(fieldIndexMap.get("lesson")));
        }

        if (fieldIndexMap.containsKey("school")) {
            dto.setSchool(data.get(fieldIndexMap.get("school")));
        }

        // 转换分数
        try {
            if (fieldIndexMap.containsKey("geoScore")) {
                String val = data.get(fieldIndexMap.get("geoScore"));
                if (val != null) {
                    dto.setGeographyScore(Double.valueOf(val));
                }
            }
        } catch (NumberFormatException e) {
         log.error("NumberFormatException",e);
        }

        if (dto.getStudentId() != null && dto.getLesson() != null && dto.getGeographyScore() != null) {
            list.add(dto);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {}
}