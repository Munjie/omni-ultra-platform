package com.munjie.omni.controller;


import com.munjie.omni.pojo.entity.SysUserEntity;
import com.munjie.omni.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/wechat")
@Tag(name = "小程序登录API")
@Slf4j
public class AuthController {

    @Resource
    private AuthService authService;


    @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode() throws Exception {
        return authService.getQrCode();
    }

    @GetMapping("/code2session")
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


    @GetMapping("/userInfo")
    public SysUserEntity getUserbyCode(@RequestParam String code) {
        return authService.getUserbyCode(code);
    }



}