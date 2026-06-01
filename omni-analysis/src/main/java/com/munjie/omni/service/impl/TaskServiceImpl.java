package com.munjie.omni.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.munjie.omni.enums.TaskStatusEnum;
import com.munjie.omni.enums.TypeEnum;
import com.munjie.omni.exception.CustomException;
import com.munjie.omni.mapper.TaskMapper;
import com.munjie.omni.pojo.dto.*;
import com.munjie.omni.pojo.entity.QuestionAnalysisEntity;
import com.munjie.omni.pojo.entity.ScoreEntity;
import com.munjie.omni.pojo.entity.TaskEntity;
import com.munjie.omni.service.ExcelService;
import com.munjie.omni.service.QuestionAnalysisService;
import com.munjie.omni.service.ScoreService;
import com.munjie.omni.service.TaskService;
import com.munjie.omni.utils.FileDownloadUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author muwenjie
 * @since 2024-10-27
 */
@Service
@Slf4j
public class TaskServiceImpl extends ServiceImpl<TaskMapper, TaskEntity> implements TaskService {


    @Resource
    private ExcelService excelService;

    @Resource
    private ScoreService scoreService;

    @Resource
    private QuestionAnalysisService questionAnalysisService;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer add(TaskDTO taskDTO, MultipartFile file) {
        TaskEntity entity = TaskEntity.builder().taskName(taskDTO.getTaskName()).email(taskDTO.getEmail()).status(TaskStatusEnum.RUNNING).build();
        this.save(entity);
        return entity.getId();
    }

    @Override
    public IPage<TaskEntity> pageTask(TaskPageDTO req) {
        LambdaQueryWrapper<TaskEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(TaskEntity::getCreateTime);
        if (StrUtil.isNotBlank(req.getTaskName())) {
            wrapper.eq(TaskEntity::getTaskName,req.getTaskName().trim());
        }
        Page<TaskEntity> pageParam = new Page<>(req.getPageNum(), req.getPageSize());
        return this.page(pageParam, wrapper);
    }

    @Override
    public void updateTask(Integer id) {
        LambdaUpdateWrapper<TaskEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TaskEntity::getId, id);
        updateWrapper.set(TaskEntity::getStatus, 2);
        this.update(updateWrapper);

    }
    @Override
    public void sendEmail(Integer id) {
    }

    @Override
    public void deleteTask(Integer id) {
        this.removeById(id);
        HashMap<String, Object> map = new HashMap<>();
        map.put("task_id", id);
        scoreService.removeByMap(map);
        questionAnalysisService.removeByMap(map);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(AnalysisDataDTO info, List<MultipartFile> files) throws IOException {
        LambdaQueryWrapper<TaskEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(info.getTaskName()), TaskEntity::getTaskName, info.getTaskName());
        List<TaskEntity> list = this.list(wrapper);
        if (CollUtil.isNotEmpty(list)) {
            throw new CustomException("任务名称已存在,重新输入其它任务名称");
        }
        TaskEntity entity = TaskEntity.builder()
                .taskName(info.getTaskName())
                .title(info.getTitle())
                .status(TaskStatusEnum.RUNNING)
                .type(TypeEnum.SCORE)
                .build();
        this.save(entity);
        Integer taskId = entity.getId();
        int sum = 0;
        int classCount = 0;
        for (MultipartFile file : files) {
            if (file.getOriginalFilename().contains("学生成绩")) {
                Integer size = scoreService.importScore(file, taskId);
                sum += size;
                classCount++;
            }
            if (file.getOriginalFilename().contains("分析")) {
                Integer size = questionAnalysisService.importData(file, taskId);

            }
            if (file.getOriginalFilename().contains("汇总")) {
                scoreService.importTotal(file);
            }
        }
        return "所有班级学生成绩导入完成,总共导入" + classCount + "个班和" + sum + "个学生";
    }

    @Override
    public ResponseEntity<ByteArrayResource> exportReport(ExportRequestDTO requestDTO, HttpServletRequest request) throws IOException {
        Integer taskId = requestDTO.getTaskId();
        String title = requestDTO.getTitle();
        LambdaQueryWrapper<ScoreEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ObjectUtil.isNotNull(taskId), ScoreEntity::getTaskId, taskId);
        List<ScoreEntity> list = scoreService.list(wrapper);
        //分析查询
        LambdaQueryWrapper<QuestionAnalysisEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ObjectUtil.isNotNull(taskId), QuestionAnalysisEntity::getTaskId, taskId);
        List<QuestionAnalysisEntity> analysisEntities = questionAnalysisService.list(lambdaQueryWrapper);
        List<FileInfoDTO> fileList = new ArrayList<>();
        File file = excelService.createExcelFile(list, analysisEntities, title);
        fileList.add(new FileInfoDTO(file.getName(), file.getAbsolutePath()));
        return FileDownloadUtil.downloadAll(file, fileList, request);
//        return FileDownloadUtil.downZipFile(fileList, title, 1, request);
    }



}



