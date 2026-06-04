package com.munjie.omni.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CertificateVO {
    private Long id;
    private String domain;
    private String status;
    private String issuer;
    private String expiryDate;
    private Boolean isWildcard;
}
