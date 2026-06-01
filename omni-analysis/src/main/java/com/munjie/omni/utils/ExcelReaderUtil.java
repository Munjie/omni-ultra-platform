package com.munjie.omni.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.ReadListener;
import com.munjie.omni.listener.DynamicHeaderListener;
import com.munjie.omni.listener.TotalListener;
import com.munjie.omni.pojo.entity.ScoreEntity;
import com.munjie.omni.pojo.entity.TotalScoreEntity;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 86158
 */
@Service
public class ExcelReaderUtil {


    public  static List<ScoreEntity> readExcel(InputStream inputStream) {
        List<ScoreEntity> resultList = new ArrayList<>();
        EasyExcel.read(inputStream, new DynamicHeaderListener(resultList))
                .headRowNumber(3)
                .sheet()
                .doRead();
        return resultList;

    }


    public  static List<TotalScoreEntity> readTotalExcel(InputStream inputStream) {

        List<TotalScoreEntity> dataList = new ArrayList<>();
        // 读取excel
        EasyExcel.read(inputStream, TotalScoreEntity.class, new TotalListener(dataList))
                .sheet()
                .headRowNumber(1)
                .doRead();
        return dataList;

    }




    public static <T> List<T> readExcel(InputStream inputStream, Class<T> clazz, ReadListener<T> listener) {
        List<T> dataList = new ArrayList<>();
        EasyExcel.read(inputStream, clazz, listener)
                .sheet()
                .headRowNumber(3)
                .doRead();
        return dataList;
    }

}