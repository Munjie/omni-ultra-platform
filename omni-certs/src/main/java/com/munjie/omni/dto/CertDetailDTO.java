package com.munjie.omni.dto;

import lombok.Data;

@Data
public class CertDetailDTO {
    private Long id;
    private String domain;
    private String status;
    private String certType;
    private String certCategory;
    private String createTime;
    
    // 主体信息
    private String issuerCn;
    private String issuerCountry;
    private String issuerProvince;
    private String issuerCity;
    private String issuerOrg;
    private String issuerOu;

    // 技术与有效期
    private String serialNumber;
    private String keyType;
    private String keyStrength;
    private String signAlgorithm;
    private String keyUsage;
    private String caUrl;
    private String crlUrl;
    private String ocspUrl;
    private String startDate;
    private String expiryDate;

    // 指纹与拓展示例
    private String sha1Fingerprint;
    private String sha256Fingerprint;
    private String sans;
    private String publicKey;
}