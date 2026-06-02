package com.munjie.omni.controller;

import cn.hutool.core.util.IdUtil;
import com.munjie.omni.pojo.dto.ExportProgress;
import com.munjie.omni.pojo.dto.ExportRequestDTO;
import com.munjie.omni.result.Result;
import com.munjie.omni.service.impl.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

@RestController
@RequestMapping("/export")
public class ExportController {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @Resource
    private ExportService exportService;

    @PostMapping("/export-report")
    public Result startExport(@RequestBody ExportRequestDTO requestDTO) {
        String businessId = String.valueOf(requestDTO.getTaskId());
        System.out.println("开始执行导出 = " + businessId);
        String lockKey = "export_lock:" + businessId;
        // 1. 防重复提交锁 (10分钟自动过期)
        Boolean lock = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofMinutes(10));
        if (Boolean.FALSE.equals(lock)) {
            return Result.error(HttpStatus.TOO_MANY_REQUESTS.value(),"报告正在生成中");
        }
        // 2. 初始化执行任务
        String exportJobId = IdUtil.getSnowflakeNextIdStr();
        redisTemplate.opsForValue().set("export_progress:" + exportJobId, new ExportProgress(0, "processing", null, null,null));
        // 3. 异步启动分析导出
        exportService.asyncExportReport(requestDTO, exportJobId);
        return Result.ok(exportJobId);
    }

    @GetMapping("/export-progress/{exportJobId}")
    public ExportProgress getProgress(@PathVariable String exportJobId) {
        System.out.println("请求exportJobId状态 = " + exportJobId);
        return (ExportProgress) redisTemplate.opsForValue().get("export_progress:" + exportJobId);
    }


    @PostMapping("/download-excel")
    @Operation(summary ="导出学生成绩质量分析")
    public ResponseEntity<ByteArrayResource> exportReport(HttpServletRequest request, @RequestBody ExportRequestDTO requestDTO) throws IOException {
        System.out.println("开始下载excel= " + request);
       return downloadAll(requestDTO.getFilePath(),requestDTO.getTitle(),request);

    }


    public static String encodeDownloadFilename(String filename, HttpServletRequest request) {
        String agent = request.getHeader("User-Agent").toUpperCase();
        try {
            // 1. Chrome, Edge, Firefox 等现代浏览器：用 RFC 5987 标准
            if (agent.contains("CHROME") || agent.contains("EDGE") ||
                    agent.contains("FIREFOX") || agent.contains("SAFARI")) {
                return "attachment; filename*=UTF-8''" +
                        URLEncoder.encode(filename, "UTF-8")
                                .replaceAll("\\+", "%20");
            }
            // 2. IE 或很老的浏览器：用普通 URL Encode
            if (agent.contains("MSIE") || agent.contains("TRIDENT")) {
                return "attachment; filename=" + URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
            }
            // 3. 其他情况：都按现代浏览器处理
            return "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
        } catch (Exception e) {
            return "attachment; filename=download.zip";
        }
    }


    public static ResponseEntity<ByteArrayResource> downloadAll(String filePath, String fileName,HttpServletRequest request) throws IOException {
        byte[] data = Files.readAllBytes(Paths.get(filePath));
        ByteArrayResource resource = new ByteArrayResource(data);
        boolean b = Files.deleteIfExists(Path.of(filePath));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                encodeDownloadFilename(fileName+".xlsx", request));
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(resource);

    }
}