package com.munjie.omni.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.munjie.omni.config.WechatSocketHandler;
import com.munjie.omni.infr.JwtTokenProvider;
import com.munjie.omni.pojo.dto.LoginReqDTO;
import com.munjie.omni.pojo.entity.SysUserEntity;
import com.munjie.omni.pojo.vo.LoginResVO;
import com.munjie.omni.service.AuthService;
import com.munjie.omni.service.SysUserService;
import com.munjie.omni.utils.CustomHttpUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Value("${wechat.mini.appid}")
    private String appid;
    @Value("${wechat.mini.secret}")
    private String secret;

    @Value("${wechat.mini.token_url}")
    private String tokenUrl;

    @Value("${wechat.mini.access_token_url}")
    private String accessTokenUrl;

    @Value("${wechat.mini.openid_url}")
    private String openidUrl;

    @Value("${wechat.mini.default_avatar}")
    private String defaultAvatar;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private SysUserService sysUserService;

    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Resource
    private PasswordEncoder passwordEncoder;


    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public ResponseEntity<byte[]> getQrCode() throws Exception {
        String url = String.format(tokenUrl, appid, secret);
        String scene = IdUtil.getSnowflakeNextIdStr();
        log.info("scene= {} ", scene);
        // 获取 access_token
        String tokenRes = CustomHttpUtil.get(url);
        String accessToken = JSONObject.parseObject(tokenRes).getString("access_token");
        System.out.println("accessToken = " + accessToken);
        String qrUrl = String.format(accessTokenUrl, accessToken);
        JSONObject param = new JSONObject();
        param.put("page", "pages/user/login");
        param.put("scene", scene);
        param.put("check_path", true);
        param.put("width", 430);
        param.put("env_version", "release");
        param.put("auto_color", false);
        JSONObject lineColor = new JSONObject();
        lineColor.put("r", 0);
        lineColor.put("g", 0);
        lineColor.put("b", 0);
        param.put("line_color", lineColor);
        byte[] qrBytes = CustomHttpUtil.postForBytes(qrUrl, param.toJSONString());
        // 存入 Redis，标记为等待扫码
        redisTemplate.opsForValue().set("scene:" + scene, "pending", 10, TimeUnit.MINUTES);
        // 额外返回 scene 给前端
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Scene", scene);
        assert qrBytes != null;
        return new ResponseEntity<>(qrBytes, headers, HttpStatus.OK);
    }


    @Override
    public Map<String, String> getOpenId(String code) {
        System.out.println("code = " + code);
        Map<String, String> result = new HashMap<>();
        try {
            String url = String.format(openidUrl, appid, secret, code);
            String res = HttpUtil.get(url);
            JSONObject jsonObject = JSONObject.parseObject(res);
            System.out.println("jsonObject = " + jsonObject.toJSONString());
            result.put("openid", jsonObject.getString("openid"));
            result.put("session_key", jsonObject.getString("session_key"));
        } catch (Exception e) {
            log.error("code2Session=", e);
            result.put("openid", "");
            result.put("session_key", "");
        }
        return result;
    }


    @Override
    public boolean bindStatus(@RequestBody Map<String, String> body) {
        System.out.println("body = " + body);
        String scene = body.get("scene");
        String key = "scene:" + scene;
        if (redisTemplate.hasKey(key)) {
            redisTemplate.opsForValue().set(key, scene + "|scanned", 10, TimeUnit.MINUTES);
            // 推送“已扫码”状态
            WechatSocketHandler.sendMessage(scene, "SCANNED");
            return true;
        }
        return false;
    }


    @Override
    public boolean confirmLogin(@RequestBody Map<String, String> body) {
        if (ObjectUtil.isNull(body)) {
            return false;
        }
        String scene = body.get("scene");
        String openid = body.get("openid");
        String avatar = body.get("avatarUrl");
        String username = body.get("nickName");
        String key = "scene:" + scene;
        String stored = redisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(stored)) {
            return false;
        }
        if (StrUtil.isBlank(username)) {
            int randomNum = ThreadLocalRandom.current().nextInt(100000);
            String randomString = String.format("%05d", randomNum);
            username = "微信用户_" + randomString;
        }
        if (StrUtil.isBlank(avatar)) {
            avatar = defaultAvatar;
        }
        SysUserEntity sysUser = SysUserEntity.builder().userName(username).avatar(avatar).openid(openid).build();
        long expire = 86400;
        if (stored.startsWith(scene + "|scanned")) {
            SysUserEntity user = sysUserService.getOrCreateUser(sysUser);
            String token = jwtTokenProvider.createToken(user.getId(), user.getUserName(), expire);
            String msg = String.format("SUCCESS|%s|%d|%s|%s|%s", token, user.getId(), user.getUserName(), user.getAvatar(), expire);
            WechatSocketHandler.sendMessage(scene, msg);
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }


    @Override
    public  SysUserEntity getUserbyCode(String code) {
        SysUserEntity user = null;
        Map<String, String> openId = getOpenId(code);
        if (CollectionUtil.isEmpty(openId) || openId.get("openid") == null) {
            return user;
        }
        String openid = openId.get("openid");
        LambdaQueryWrapper<SysUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserEntity::getOpenid, openid);
        user = sysUserService.getOne(wrapper);
        if (ObjectUtil.isNotNull(user)) {
            user.setRegistered(true);
        }else {
            user  = new SysUserEntity();
            user.setOpenid(openid);
        }
        return user;
    }

    @Override
    public LoginResVO login(LoginReqDTO request) {
        LambdaQueryWrapper<SysUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserEntity::getUserName, request.getUsername());
        SysUserEntity sysUser = sysUserService.getOne(wrapper);
        if (sysUser == null || !passwordEncoder.matches(request.getPassword(), sysUser.getPassword())) {
//            throw new CustomException("用户名或密码错误");
        }
        long expire = 86400; // 1天有效时间
        String token = jwtTokenProvider.createToken(sysUser.getId(), sysUser.getUserName(), expire);
        return LoginResVO.builder()
                .userId(sysUser.getId())
                .username(sysUser.getUserName())
                .avatar(sysUser.getAvatar())
                .token(token)
                .expire(expire)
                .build();
    }

    @Override
    public String logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String tokenValue = jwt.getTokenValue();
            Instant expiresAt = jwt.getExpiresAt();
            if (expiresAt != null) {
                long remainingSeconds = ChronoUnit.SECONDS.between(Instant.now(), expiresAt);
                if (remainingSeconds > 0) {
                    stringRedisTemplate.opsForValue().set(
                            "jwt:blacklist:" + tokenValue,
                            "logout_status",
                            remainingSeconds,
                            TimeUnit.SECONDS
                    );
                }
            }
        }
        return "登出成功";
    }
}
