package com.munjie.omni.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportProgress implements Serializable {
    private int percent;      // 0-100
    private String status;     // processing, completed, failed
    private String fileId;     // 存储在临时目录的文件名
    private String currentStage;
    private String errorMsg;   // 报错信息
}