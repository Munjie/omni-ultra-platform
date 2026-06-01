package com.munjie.omni.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.munjie.omni.pojo.dto.ScorePageDTO;
import com.munjie.omni.pojo.entity.ScoreEntity;
import com.munjie.omni.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author mwj
 * @since 2023-11-23
 */
public interface ScoreService extends IService<ScoreEntity> {


    Integer importScore(MultipartFile file, Integer taskId) throws IOException;

    Integer importTotal(MultipartFile file) throws IOException;

    IPage<ScoreEntity> pageScore(ScorePageDTO pageDTO);

    List<String> listLesson();

    Result delete(String name, String number, String lesson);

}
