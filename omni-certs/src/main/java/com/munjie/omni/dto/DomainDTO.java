package com.munjie.omni.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DomainDTO {

    @NotBlank(message = "域名不能为空")
    private String domain;
}
