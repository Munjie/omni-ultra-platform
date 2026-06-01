package com.munjie.omni.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.munjie.omni.mapper.ScoreMapper;
import com.munjie.omni.pojo.dto.ScorePageDTO;
import com.munjie.omni.pojo.entity.QuestionAnalysisEntity;
import com.munjie.omni.pojo.entity.ScoreEntity;
import com.munjie.omni.pojo.entity.TotalScoreEntity;
import com.munjie.omni.result.Result;
import com.munjie.omni.service.QuestionAnalysisService;
import com.munjie.omni.service.ScoreService;
import com.munjie.omni.utils.ExcelReaderUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author mwj
 * @since 2023-11-23
 */
@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, ScoreEntity> implements ScoreService {


    @Resource
    private QuestionAnalysisService questionAnalysisService;

    @Resource
    private TotalScoreExportService totalScoreExportService;

    @Override
    public Integer importScore(MultipartFile file,Integer taskId) throws IOException {
        List<ScoreEntity> scoreEntities = ExcelReaderUtil.readExcel(file.getInputStream());
        if (CollectionUtil.isNotEmpty(scoreEntities)) {
            scoreEntities.forEach(m -> m.setTaskId(taskId));
            this.saveBatch(scoreEntities);
        }
        return scoreEntities.size();
    }

    @Override
    public Integer importTotal(MultipartFile file) throws IOException {
        List<TotalScoreEntity> totalScoreEntities = ExcelReaderUtil.readTotalExcel(file.getInputStream());
        totalScoreExportService.exportScoreTip(totalScoreEntities);
//        service.saveBatch(totalScoreEntities);
        return totalScoreEntities.size();
    }

    @Override
    public IPage<ScoreEntity> pageScore(ScorePageDTO pageDTO) {
        LambdaQueryWrapper<ScoreEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ScoreEntity::getId);
        wrapper.like(StrUtil.isNotBlank(pageDTO.getName()),ScoreEntity::getName,pageDTO.getName());
        wrapper.like(StrUtil.isNotBlank(pageDTO.getLesson()),ScoreEntity::getLesson,pageDTO.getLesson());
        wrapper.eq(ObjectUtil.isNotNull(pageDTO.getTaskId()),ScoreEntity::getTaskId,pageDTO.getTaskId());
        Page<ScoreEntity> pageParam = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        return this.page(pageParam, wrapper);
    }

    @Override
    public List<String> listLesson() {
        LambdaQueryWrapper<ScoreEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ScoreEntity::getLesson);
        wrapper.select(ScoreEntity::getLesson);
        List<ScoreEntity> list = this.list(wrapper);
        return list.stream()
                .map(ScoreEntity::getLesson)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Result delete(String name, String number, String lesson) {
        LambdaQueryWrapper<ScoreEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(name),ScoreEntity::getName,name);
        wrapper.like(StrUtil.isNotBlank(number),ScoreEntity::getStudentId,number);
        wrapper.like(StrUtil.isNotBlank(lesson),ScoreEntity::getLesson,lesson);
        boolean remove = this.remove(wrapper);
        //
        LambdaQueryWrapper<QuestionAnalysisEntity> wa = new LambdaQueryWrapper<>();
        wa.like(StrUtil.isNotBlank(name),QuestionAnalysisEntity::getClassName,lesson);
        questionAnalysisService.remove(wa);
        return remove ? Result.ok("删除成功") : Result.error("失败");
    }
}
