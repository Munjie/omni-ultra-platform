package com.munjie.omni.infr;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.munjie.omni.config.UserContext;
import com.munjie.omni.result.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class UserContextFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;


    public UserContextFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                String tokenValue = jwt.getTokenValue();
                Boolean isBlacklisted = redisTemplate.hasKey("jwt:blacklist:" + tokenValue);
                if (isBlacklisted) {
                    SecurityContextHolder.clearContext();
                    writeUnAuth(response, "您的登录已注销，请重新登录");
                    return;
                }
                String userIdStr = jwt.getSubject();
                if (StrUtil.isNotBlank(userIdStr)) {
                    UserContext.setUserId(Integer.valueOf(userIdStr));
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    private void writeUnAuth(HttpServletResponse response, String msg) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(JSON.toJSONString(Result.error(401, msg)));
    }
}