package com.munjie.omni.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.munjie.omni.pojo.dto.AnalysisDataDTO;
import com.munjie.omni.pojo.dto.ExportRequestDTO;
import com.munjie.omni.pojo.dto.ScorePageDTO;
import com.munjie.omni.pojo.dto.TaskPageDTO;
import com.munjie.omni.pojo.entity.ScoreEntity;
import com.munjie.omni.pojo.entity.TaskEntity;
import com.munjie.omni.result.Result;
import com.munjie.omni.service.ScoreService;
import com.munjie.omni.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author muwen
 */
@RestController
@RequestMapping("/score-manage")
@Tag(name = "学生成绩管理API")
@Slf4j
public class ScoreManageController {

    @Resource
    private ScoreService scoreService;


    @Resource
    private TaskService taskService;


    @PostMapping("/page-task")
    @Operation(summary ="分页查询成绩任务")
    public IPage<TaskEntity> pageTask(@RequestBody TaskPageDTO taskPageDTO) {
        return taskService.pageTask(taskPageDTO);
    }


    @DeleteMapping(value="/delete-task")
    public String deleteTask(@RequestParam("taskId") Integer taskId) {
        taskService.deleteTask(taskId);
        return "删除成功";
    }

    @PostMapping("/create-score-task")
    @Operation(summary ="创建成绩分析任务")
    public String create(@RequestPart AnalysisDataDTO info, List<MultipartFile> files) throws IOException {
        return  taskService.create(info,files);
    }


    @PostMapping("/export-report")
    @Operation(summary ="导出学生成绩质量分析")
    public ResponseEntity<ByteArrayResource> exportReport(HttpServletRequest request, @RequestBody ExportRequestDTO requestDTO) throws IOException {
        return taskService.exportReport(requestDTO,request);
    }


    @PostMapping("/page-score")
    @Operation(summary ="分页查询成绩")
    public IPage<ScoreEntity> pageTask(@RequestBody ScorePageDTO scorePageDTO) {
        return scoreService.pageScore(scorePageDTO);
    }

    @GetMapping("/list-lesson")
    @Operation(summary ="查询班级列表")
    public List<String> listLesson() {
        return scoreService.listLesson();
    }



    @GetMapping("/delete")
    @Operation(summary ="删除")
    public Result delete(@RequestParam(defaultValue = "") String name, @RequestParam(defaultValue = "") String number, @RequestParam(defaultValue = "") String lesson) {
        return scoreService.delete(name,number,lesson);
    }


}
