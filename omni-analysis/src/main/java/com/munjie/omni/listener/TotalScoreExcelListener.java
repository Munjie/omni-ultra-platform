package com.munjie.omni.listener;


import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.holder.ReadSheetHolder;
import com.alibaba.excel.util.ListUtils;
import com.munjie.omni.pojo.entity.TotalScoreEntity;
import com.munjie.omni.service.TotalScoreService;
import com.nimbusds.jose.shaded.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


/**
 * @author 86158
 */
@Slf4j
public class TotalScoreExcelListener extends AnalysisEventListener<TotalScoreEntity> {

    TotalScoreService service;
    int BATCH_COUNT;
    int total = 0;
    private String currentSheetName;

    /**
     *构造方法传参
     * @param service
     * @param size
     */
    public TotalScoreExcelListener(TotalScoreService service, int size){
        this.service = service;
        this.BATCH_COUNT = size;
    }
 
    /**
     * 缓存的数据 BATCH_COUNT为传参所赋值
     */
    private List<TotalScoreEntity> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
 
 
    /**
     * 每一条数据解析都会调用
     * @param data
     * @param analysisContext
     */
    @Override
    public void invoke(TotalScoreEntity data, AnalysisContext analysisContext) {
        if (currentSheetName == null) {
            ReadSheetHolder sheetHolder = analysisContext.readSheetHolder();
            currentSheetName = sheetHolder.getSheetName();
        }
        total++;
        log.info("解析到一条数据:{}", new Gson().toJson(data));
        cachedDataList.add(data);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            currentSheetName = null;
            // 存储完成清理 list
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }
 
 
 
 
    /**
     * 所有数据解析完成了 都会来调用（sheet页）注意保存完之后要清空下，否则会出现第二个sheet页内容录入到第一个Sheet页对应表中
     *
     * @param
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        currentSheetName = null;
        cachedDataList.clear();
        log.info("所有数据解析完成！:" + total);
    }
 

    private void saveData() {
        System.out.println(cachedDataList.size()+"条数据，开始存储数据库！");
        cachedDataList.forEach(totalScoreEntity -> {
            totalScoreEntity.setTotalScore(totalScoreEntity.getChinese()
                + totalScoreEntity.getMath()
                + totalScoreEntity.getEnglish()
                +totalScoreEntity.getPhysics()
                    +totalScoreEntity.getPolitics()
                    +totalScoreEntity.getHistory()
                    +totalScoreEntity.getGeography()
                    +totalScoreEntity.getBiology());});
        service.saveBatch(cachedDataList);
    }

    public Integer getData() {
        return total;
    }

}