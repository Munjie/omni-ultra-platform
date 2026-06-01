package com.munjie.omni.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.munjie.omni.exception.CustomException;
import com.munjie.omni.listener.ExcelReadListener;
import com.munjie.omni.mapper.QuestionAnalysisMapper;
import com.munjie.omni.pojo.entity.QuestionAnalysisEntity;
import com.munjie.omni.service.QuestionAnalysisService;
import com.munjie.omni.utils.ExcelReaderUtil;
import com.munjie.omni.utils.NumberExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 试题分析表 服务实现类
 * </p>
 *
 * @author muwenjie
 * @since 2024-06-20
 */
@Service
public class QuestionAnalysisServiceImpl extends ServiceImpl<QuestionAnalysisMapper, QuestionAnalysisEntity> implements QuestionAnalysisService {

    @Override
    @Transactional
    public int importData(MultipartFile file,Integer taskId){
        List<QuestionAnalysisEntity> dataList = new ArrayList<>();
        String originalFilename = file.getOriginalFilename();
        String classId = NumberExtractor.extractNumbers(originalFilename);
        if (StrUtil.isBlank(classId)) {
            throw new CustomException("导入的excel文件名必须要包含班级名称，是数字班级名称");
        }
        try {
          ExcelReaderUtil.readExcel(file.getInputStream(), QuestionAnalysisEntity.class, new ExcelReadListener<>(dataList));
        } catch (IOException e) {
           throw new CustomException("解析excel异常");
        }
        dataList.stream().forEach(p -> {
            p.setClassName(classId + "班");
            p.setTaskId(taskId);
            p.setSubQuestionNumber(Integer.valueOf(Objects.requireNonNull(NumberExtractor.extractNumbers(p.getQuestionNumber()))));
        });
        this.saveBatch(dataList);
        return dataList.size();
    }
}

