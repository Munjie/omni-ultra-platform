package com.munjie.omni.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertChallengeDTO {
    private Long id;
    private String domain;
    private String hostRecord;
    private String recordValue;
    private String recordType;
    private String status;
    private String errorMessage;
}

