package com.munjie.omni.constant;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "wx")
public class WxConfig {

    private Auth auth;
    private Template template;

    // 认证配置内部类
    @Setter
    @Getter
    public static class Auth {
        private String grantType;
        private String appid;
        private String secret;
        private String tokenUrl;

    }

    // 模板消息配置内部类
    @Setter
    @Getter
    public static class Template {
        // getter和setter
        private String sendUrl;
        private String templateId;

    }

}