package com.munjie.omni.handler;

import com.munjie.omni.exception.CustomException;
import com.munjie.omni.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * @Date 2023/12/25 14:55
 * @Author mwj
 **/
@Slf4j
@RestControllerAdvice
//@Hidden
public class GlobalExceptionHandler {


    @Value("${blog.url}")
    private String logUrl;

    /**
     * @description: 全局Exception
     **/

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("不支持该请求方法: {}", e.getMethod());
        return Result.error(405, "请求方法不支持: " + e.getMethod());
    }



    @ExceptionHandler(CustomException.class)
    public Result<Void> handleCustomException(CustomException e, HttpServletRequest request) {
        log.error("系统发生异常RuntimeException: {}", e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage());
    }



    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("资源不存在: {}", e.getResourcePath(),e);
        return Result.error(404, e.getMessage());
    }

    @ExceptionHandler(ClientAbortException.class)
    public Result<Void> handleClientAbortException(NoResourceFoundException e) {
        log.warn("服务器请求错误: {}", e.getResourcePath(),e);
        return Result.error(500, e.getMessage());
    }




    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统未知错误Exception, URL: {}", request.getRequestURI(), e);
        return Result.error(500,e.getMessage());
    }


    public String buildUrl() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String logTime = now.format(formatter);
        String encodedTime = URLEncoder.encode(logTime, StandardCharsets.UTF_8);
        return logUrl + "/log?time=" + encodedTime;


    }
}
