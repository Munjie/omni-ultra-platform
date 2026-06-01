package com.munjie.omni.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.munjie.omni.pojo.entity.QuestionAnalysisEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 试题分析表 服务类
 * </p>
 *
 * @author muwenjie
 * @since 2024-06-20
 */
public interface QuestionAnalysisService extends IService<QuestionAnalysisEntity> {

    int importData(MultipartFile file,Integer taskId);

}
