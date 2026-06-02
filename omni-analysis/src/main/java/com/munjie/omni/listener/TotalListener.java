package com.munjie.omni.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.munjie.omni.pojo.entity.TotalScoreEntity;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author 86158
 */
@AllArgsConstructor
public class TotalListener extends AnalysisEventListener<TotalScoreEntity> {

    private List<TotalScoreEntity> dataList;

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        for (Integer key : headMap.keySet()) {
            System.out.print(key + ":" + headMap.get(key) + "\t");
        }
        System.out.println();
    }

    @Override
    public void invoke(TotalScoreEntity data, AnalysisContext context) {
        dataList.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
    }

}