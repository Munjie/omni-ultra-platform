package com.munjie.omni.service;

import org.springframework.web.multipart.MultipartFile;

public interface SystemService {


    /**
     * sftp upload
     * @param file
     * @return
     */
     String uploadFile(MultipartFile file);


}
