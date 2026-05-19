package com.munjie.omni.handler;


import com.alibaba.fastjson2.JSON;
import com.munjie.omni.annotation.NoWrap;
import com.munjie.omni.result.Result;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.annotation.Annotation;

@RestControllerAdvice(basePackages = "com.munjie.omni.controller")
public class RestResult implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Annotation noWrap = returnType.getMethodAnnotation(NoWrap.class);
        return noWrap == null;


    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof String) {
            return JSON.toJSONString(Result.ok(body));
        }
        if (body instanceof Result) {
            return body;
        }
        if (body instanceof ResponseEntity && ((ResponseEntity<?>) body).getBody() instanceof byte[]) {
            return body;
        }
        // If the body is byte[] type, return it directly
        if (body instanceof byte[] || body instanceof ByteArrayResource) {
            return body;
        }
        if (body instanceof InputStreamResource) {
            return body;
        }
        //封装后的数据返回到前端页面
        return Result.ok(body);
    }

}
