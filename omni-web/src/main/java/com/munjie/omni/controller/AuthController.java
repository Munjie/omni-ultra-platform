package com.munjie.omni.controller;


import com.munjie.omni.annotation.RateLimit;
import com.munjie.omni.pojo.dto.LoginReqDTO;
import com.munjie.omni.pojo.entity.SysUserEntity;
import com.munjie.omni.pojo.vo.LoginResVO;
import com.munjie.omni.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Tag(name = "小程序登录API")
@Slf4j
public class AuthController {

    @Resource
    private AuthService authService;


    @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode() throws Exception {
        return authService.getQrCode();
    }

    @GetMapping("/get-open-id")
    public  Map<String, String> getOpenId(@RequestParam String code)  {
        return authService.getOpenId(code);
    }


    @PostMapping("/bind")
    public boolean bind(@RequestBody Map<String, String> body) {
        return authService.bindStatus(body);
    }


    @PostMapping("/confirm")
    public boolean confirmLogin(@RequestBody Map<String, String> body) {
        return authService.confirmLogin(body);
    }


    @GetMapping("/get-user-info")
    public SysUserEntity getUserbyCode(@RequestParam String code) {
        return authService.getUserbyCode(code);
    }


    @Operation(summary ="登录")
    @PostMapping("/login")
    public LoginResVO login(@RequestBody LoginReqDTO user) {
        return authService.login(user);
    }

    @Operation(summary ="登出")
    @GetMapping("/logout")
    public String logout() {
        return authService.logout();

    }





}