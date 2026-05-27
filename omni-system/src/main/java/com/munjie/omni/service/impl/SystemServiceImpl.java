package com.munjie.omni.service.impl;

import com.munjie.omni.exception.CustomException;
import com.munjie.omni.infr.SftpUploader;
import com.munjie.omni.service.SystemService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;


@Service
@Slf4j
public class SystemServiceImpl implements SystemService {

    @Resource
    private SftpUploader sftpUploader;

    @Value("${sftp.base-url}")
    private String imageBaseUrl;

    @Override
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            return "文件不能为空";
        }
        try {
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + fileExtension;
            InputStream inputStream = file.getInputStream();
            sftpUploader.uploadFile(newFilename, inputStream);
            return imageBaseUrl + newFilename;
        } catch (Exception e) {
           throw new CustomException(e.getMessage());
        }
    }
}
