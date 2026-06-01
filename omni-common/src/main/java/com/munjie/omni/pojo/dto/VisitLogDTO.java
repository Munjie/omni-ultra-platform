package com.munjie.omni.pojo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class VisitLogDTO {
    private String ip;
    private String url;
    private String userAgent;
    private Date visitTime;
}