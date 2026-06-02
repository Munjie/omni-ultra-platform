package com.munjie.omni.listener;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.munjie.omni.pojo.entity.QuestionAnalysisEntity;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class ExcelReadListener<T> extends AnalysisEventListener<T> {

    private List<T> dataList;

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        for (Integer key : headMap.keySet()) {
            System.out.print(key + ":" + headMap.get(key) + "\t");
        }
        System.out.println();
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        if(data instanceof QuestionAnalysisEntity) {
            QuestionAnalysisEntity entity = (QuestionAnalysisEntity) data;
            if (ObjectUtil.isEmpty(entity.getSubQuestionNumber())) {
                entity.setSubQuestionNumber(0);
            }}
        dataList.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 分析结束后的处理
    }
}