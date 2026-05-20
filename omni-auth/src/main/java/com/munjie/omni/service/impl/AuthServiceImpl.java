package com.munjie.omni.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.munjie.omni.config.WechatSocketHandler;
import com.munjie.omni.infr.JwtTokenProvider;
import com.munjie.omni.pojo.dto.LoginReqDTO;
import com.munjie.omni.pojo.entity.SysUserEntity;
import com.munjie.omni.pojo.vo.LoginResVO;
import com.munjie.omni.service.AuthService;
import com.munjie.omni.service.SysUserService;
import com.munjie.omni.utils.CustomHttpUtil;
import com.munjie.omni.utils.LoginHttpUtil;
import io.netty.channel.ConnectTimeoutException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
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
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {


    private static final ObjectMapper objectMapper = new ObjectMapper();

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

    @Value("${gitee.client-id}")
    private String giteeClientId;

    @Value("${gitee.client-secret}")
    private String giteeClientSecret;

    @Value("${gitee.redirect-uri}")
    private String giteeRedirectUri;

    @Value("${gitee.auth-url}")
    private String giteeAuthUrl;

    @Value("${gitee.token-url}")
    private String giteeTokenUrl;

    @Value("${gitee.user-url}")
    private String giteeUserUrl;


    @Value("${qq.auth-url}")
    private String qqAuthUrl;

    @Value("${qq.client-id}")
    private String qqClientId;

    @Value("${qq.client-secret}")
    private String qqClientSecret;

    @Value("${qq.redirect-uri}")
    private String qqRedirectUri;

    @Value("${qq.token-url}")
    private String qqTokenUrl;

    @Value("${qq.openid-url}")
    private String qqOpenIdUrl;

    @Value("${qq.user-url}")
    private String qqUserUrl;

    @Value("${github.clientId}")
    private String githubClientId;

    @Value("${github.secrets}")
    private String secrets;

    @Value("${github.auth-url}")
    private String githubAuthUrl;

    @Value("${github.token-url}")
    private String githubTokenUrl;

    @Value("${github.user-url}")
    private String githubUserUrl;


    @Value("${blog.url}")
    private String blogUrl;

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

    @Resource
    private LoginHttpUtil httpUtil;


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

    @Override
    public RedirectView giteeCallback(String code, String state) {
        try {
            System.out.println("gitee/callback code = " + code);
            String tokenResponse = getGiteeAccessToken(code);
            JsonNode rootNode = objectMapper.readTree(tokenResponse);
            String accessToken = rootNode.get("access_token").asText();
            String userResponse = getUserInfoByToken(accessToken, giteeUserUrl);
            SysUserEntity user = parseUser(userResponse);
            String redirectUrl = blogUrl;
            if (user != null) {
                user = sysUserService.getOrCreateUser(user);
                String token = jwtTokenProvider.createToken(user.getId(), user.getUserName(), 86400);
                redirectUrl = UriComponentsBuilder.fromHttpUrl(blogUrl + "/oauth/callback")
                        .queryParam("token", token)
                        .queryParam("userId", user.getId())
                        .queryParam("username", user.getUserName())
                        .queryParam("avatar", user.getAvatar())
                        .queryParam("state", state)
                        .build().encode().toUriString();
            }
            return new RedirectView(redirectUrl);
        } catch (Exception e) {
            log.error("Gitee登录失败", e);
            String errorMsg = e.getMessage();
            if (e.getMessage().contains("access_denied")) {
                errorMsg = "您取消了Gitee授权";
            }
            try {
                String encodedMsg = URLEncoder.encode(errorMsg, StandardCharsets.UTF_8.toString());
                return new RedirectView(blogUrl + "/login?error=" + encodedMsg);
            } catch (UnsupportedEncodingException ex) {
                return new RedirectView(blogUrl + "/login?error=auth_failed");
            }

        }
    }


    private String getGiteeAccessToken(String code) throws Exception {
        try (CloseableHttpClient httpClient = httpUtil.createHttpClient()) {
            HttpPost httpPost = new HttpPost(giteeTokenUrl);
            httpPost.setConfig(httpUtil.getRequestConfig());
            String requestBody = String.format("client_id=%s&client_secret=%s&code=%s&redirect_uri=%s&grant_type=authorization_code",
                    URLEncoder.encode(giteeClientId, StandardCharsets.UTF_8),
                    URLEncoder.encode(giteeClientSecret, StandardCharsets.UTF_8),
                    URLEncoder.encode(code, StandardCharsets.UTF_8),
                    URLEncoder.encode(giteeRedirectUri, StandardCharsets.UTF_8));
            httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setHeader("Accept", "application/json");
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        } catch (ConnectTimeoutException | SocketTimeoutException e) {
            log.error("Gitee API 响应超时: {}", e.getMessage());
            throw new RuntimeException("网络连接超时,请检查你的网络");
        } catch (Exception e) {
            log.error("获取 Gitee AccessToken 失败: {}", e.getMessage());
            throw e;
        }
    }

    private String getUserInfoByToken(String accessToken, String url) throws Exception {
        try (CloseableHttpClient httpClient = httpUtil.createHttpClient()) {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Authorization", "token " + accessToken);
            httpGet.setHeader("Accept", "application/json");
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        }
    }

    public SysUserEntity parseUser(String rawJson) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(rawJson);
        String username = rootNode.get("login").asText();
        long id = rootNode.get("id").asLong();
        String avatarUrl = rootNode.get("avatar_url").asText();
        System.out.println("用户名: " + username);
        System.out.println("ID: " + id);
        System.out.println("头像地址: " + avatarUrl);
        return SysUserEntity.builder().userName(username).avatar(avatarUrl).openid(String.valueOf(id)).build();
    }

    @Override
    public RedirectView qqCallback(String code, String state) {
        System.out.println("1. QQ Code = " + code);
        String redirectUrl = blogUrl;
        try {
            String qqToken = getQQToken(code);
            JSONObject qqOpenId = getQQOpenId(qqToken);
            String consumerKey = qqOpenId.getString("client_id");
            String openid = qqOpenId.getString("openid");
            JSONObject userRealInfo =getQQUserInfo(qqToken,consumerKey,openid);
            SysUserEntity user = SysUserEntity.builder().build();
            user.setOpenid(openid);
            user.setUserName(userRealInfo.getString("nickname"));
            user.setAvatar(userRealInfo.getString("figureurl_qq"));
            if (StrUtil.isNotBlank(user.getOpenid())) {
                user = sysUserService.getOrCreateUser(user);
                String token = jwtTokenProvider.createToken(user.getId(), user.getUserName(), 86400);
                redirectUrl = UriComponentsBuilder.fromHttpUrl(blogUrl + "/oauth/callback")
                        .queryParam("token", token)
                        .queryParam("userId", user.getId())
                        .queryParam("username", user.getUserName())
                        .queryParam("avatar", user.getAvatar())
                        .queryParam("state", state)
                        .build().encode().toUriString();
            }

            return new RedirectView(redirectUrl);
        } catch (Exception e) {
            log.error("QQ登录失败", e);
            String errorMsg = e.getMessage();
            if (e.getMessage().contains("access_denied")) {
                errorMsg = "您取消了QQ授权";
            }
            try {
                String encodedMsg = URLEncoder.encode(errorMsg, StandardCharsets.UTF_8.toString());
                return new RedirectView(blogUrl + "/login?error=" + encodedMsg);
            } catch (UnsupportedEncodingException ex) {
                return new RedirectView(blogUrl + "/login?error=auth_failed");
            }
        }
    }

    private String getQQToken(String code) throws Exception {
        String accessTaken="";
        String url = String.format("%s?client_id=%s&client_secret=%s&code=%s&redirect_uri=%s&grant_type=authorization_code",
                qqTokenUrl,
                URLEncoder.encode(qqClientId, StandardCharsets.UTF_8),
                URLEncoder.encode(qqClientSecret, StandardCharsets.UTF_8),
                URLEncoder.encode(code, StandardCharsets.UTF_8),
                URLEncoder.encode(qqRedirectUri, StandardCharsets.UTF_8));
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseString = response.body().string();
                accessTaken = responseString.split("=")[1].split("&")[0];
                System.out.println("QQ返回accessTaken="+accessTaken);
            }
        }
        return accessTaken;

    }


    public JSONObject getQQOpenId(String accessToken) throws IOException {
        JSONObject userInfo = new JSONObject();
        String url = String.format("%s?access_token=%s",
                qqOpenIdUrl,
                accessToken);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String UserInfoString = response.body().string().split(" ")[1];
                userInfo = JSONObject.parseObject(UserInfoString);
                System.out.println("QQ返回openid="+userInfo);
            }
        }
        return userInfo;
    }


    public JSONObject getQQUserInfo(String accessToken , String consumerKey , String openid ) throws IOException {
        JSONObject userRealInfo = new JSONObject();
        String url = String.format("%s?access_token=%s&oauth_consumer_key=%s&openid=%s",
                qqUserUrl,
                accessToken,
                consumerKey,
                openid
        );
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String UserRealInfoString = response.body().string();
                userRealInfo = JSONObject.parseObject(UserRealInfoString);
                System.out.println("QQ返回用户信息："+userRealInfo);
            }
        }
        return userRealInfo;
    }


    @Override
    public RedirectView gitHubLogin(String code, String state, HttpServletResponse response) {
        try {
            System.out.println("code = " + code);
            String accessToken = getHubToken(code);
            System.out.println("accessToken = " + accessToken);
            String userInfo = getUserInfoByToken(accessToken, githubUserUrl);
            System.out.println("userInfo = " + userInfo);
            SysUserEntity user = parseUser(userInfo);
            String redirectUrl = blogUrl;
            if (user != null) {
                user = sysUserService.getOrCreateUser(user);
                String token = jwtTokenProvider.createToken(user.getId(), user.getUserName(), 86400);
                redirectUrl = UriComponentsBuilder.fromHttpUrl(blogUrl + "/oauth/callback")
                        .queryParam("token", token)
                        .queryParam("userId", user.getId())
                        .queryParam("username", user.getUserName())
                        .queryParam("avatar", user.getAvatar())
                        .queryParam("state", state)
                        .build().encode().toUriString();
            }
            return new RedirectView(redirectUrl);
        } catch (Exception e) {
            log.error("GitHub登录失败", e);
            String errorMsg = "GitHub登录失败，请稍后再试";
//            String errorMsg = e.getMessage();
            if (e.getMessage().contains("access_denied")) {
                errorMsg = "您取消了授权";
            }
            try {
                String encodedMsg = URLEncoder.encode(errorMsg, StandardCharsets.UTF_8.toString());
                return new RedirectView(blogUrl + "/login?error=" + encodedMsg);
            } catch (UnsupportedEncodingException ex) {
                return new RedirectView(blogUrl + "/login?error=auth_failed");
            }

        }
    }


    public String getHubToken(String code) throws Exception {
        try (CloseableHttpClient httpClient = httpUtil.createHttpClient()) {
            HttpPost postRequest = new HttpPost(githubTokenUrl);
            postRequest.setConfig(httpUtil.getRequestConfig());
            postRequest.setHeader("Content-Type", "application/x-www-form-urlencoded");
            postRequest.setHeader("Accept", "application/json");
            postRequest.setHeader("User-Agent", "Java-HttpClient/munjie-blog");
            StringEntity params = new StringEntity(
                    "client_id=" + githubClientId +
                            "&client_secret=" + secrets +
                            "&code=" + code
            );
            postRequest.setEntity(params);
            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                HttpEntity entity = response.getEntity();
                String string = EntityUtils.toString(entity);
                System.out.println("access_token = " + string);
                return extractValue(string, "access_token");
            }
        }
    }

    private String extractValue(String json, String key) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(json);
        return root.path(key).asText();
    }


    @Override
    public RedirectView loginAuth(String platform, String redirect, HttpServletRequest request) {
        String authUrl = "";
        switch (platform.toLowerCase()) {
            case "github":
                authUrl = String.format("%s?client_id=%s&state=%s",  githubAuthUrl, encode(githubClientId), encode(redirect));
                break;
            case "gitee":
                if (!httpUtil.isReachable("https://gitee.com")) {
                    return checkConnectResult(request, platform);
                }
                authUrl = String.format("%s?client_id=%s&redirect_uri=%s&scope=user_info&state=%s&response_type=code",
                        giteeAuthUrl,
                        encode(giteeClientId),
                        encode(giteeRedirectUri),
                        encode(redirect));
                break;
            case "qq":
                authUrl = String.format("%s?client_id=%s&redirect_uri=%s&state=%s&response_type=code",
                        qqAuthUrl,
                        encode(qqClientId),
                        encode(qqRedirectUri),
                        encode(redirect));
                break;
            default:
                return redirectToError(redirect, "不支持的登录方式");
        }
        return new RedirectView(authUrl);
    }

    public RedirectView checkConnectResult(HttpServletRequest request, String platform) {
        log.error("后端服务器无法访问,{}", platform);
        try {
            String errorMsg = URLEncoder.encode("连接" + platform + "失败，请稍后重试", "UTF-8");
            String redirectParam = request.getParameter("redirect");
            String target = blogUrl + "/login?error=" + errorMsg;
            if (redirectParam != null) {
                target += "&redirect=" + URLEncoder.encode(redirectParam, "UTF-8");
            }
            return new RedirectView(target);
        } catch (Exception e) {
            return new RedirectView(blogUrl + "/login?error=network_error");
        }
    }


    private RedirectView redirectToError(String redirect, String msg) {
        String target = String.format("%s/login?error=%s&redirect=%s",
                blogUrl, encode(msg), encode(redirect));
        return new RedirectView(target);
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}
