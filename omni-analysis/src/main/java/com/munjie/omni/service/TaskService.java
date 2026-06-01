package com.munjie.omni.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.munjie.omni.pojo.dto.AnalysisDataDTO;
import com.munjie.omni.pojo.dto.ExportRequestDTO;
import com.munjie.omni.pojo.dto.TaskDTO;
import com.munjie.omni.pojo.dto.TaskPageDTO;
import com.munjie.omni.pojo.entity.TaskEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author muwenjie
 * @since 2024-10-27
 */
public interface TaskService extends IService<TaskEntity> {

    Integer add(TaskDTO taskDTO, MultipartFile file);

    IPage<TaskEntity> pageTask(TaskPageDTO taskPageDTO);

    void updateTask(Integer id);

    void sendEmail(Integer id);

    void deleteTask(Integer id);

    ResponseEntity<ByteArrayResource> exportReport(ExportRequestDTO requestDTO, HttpServletRequest request) throws IOException;

    String create(AnalysisDataDTO info, List<MultipartFile> files) throws IOException;




}
