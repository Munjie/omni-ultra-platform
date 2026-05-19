package com.munjie.omni.infr;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JwtTokenProvider {

    private final JwtEncoder jwtEncoder;

    public JwtTokenProvider(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    /**
     * 生成符合 Spring Security OAuth2 规范的标准 JWT 令牌
     */
    public String createToken(Long userId, String username, long expireSeconds) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("https://munjie.com")                 // 签发者
                .issuedAt(now)                               // 签发时间
                .expiresAt(now.plusSeconds(expireSeconds))   // 过期时间
                .subject(String.valueOf(userId))             // 面向主体 (通常存放唯一的用户ID)
                .claim("username", username)                 // 自定义负载
                .claim("scope", "ROLE_USER")                 // 赋予默认角色/权限范围
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}