package com.munjie.omni.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "用户登录响应数据")
public class LoginResVO {
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "用户头像")
    private String avatar;
    @Schema(description = "访问Token")
    private String token;
    @Schema(description = "有效时间(秒)")
    private long expire;
}