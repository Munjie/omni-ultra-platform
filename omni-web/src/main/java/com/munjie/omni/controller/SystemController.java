package com.munjie.omni.controller;

import com.munjie.omni.pojo.entity.SysMenuItemEntity;
import com.munjie.omni.service.AuthService;
import com.munjie.omni.service.SysMenuItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

/**
 * @Date 2023/11/23 19:58
 * @Author mwj
 **/
@RestController
@RequestMapping("/system")
@Tag(name = "系统管理")
@Slf4j
public class SystemController {




    @Resource
    private SysMenuItemService menuItemService;

    @Resource
    private AuthService authService;




    @GetMapping("/list-menu/{userId}")
    @Operation(summary ="查询用户菜单")
    public List<SysMenuItemEntity> listMenuById(@PathVariable("userId") Integer userId) {
        return menuItemService.listMenuById(userId);
    }

    @GetMapping("/gitee/callback")
    public RedirectView giteeCallback(String code, String state) {
        return authService.giteeCallback(code, state);
    }

    @GetMapping("/qq/callback")
    public RedirectView qqCallback(String code, String state) {
        return authService.qqCallback(code, state);
    }

    @GetMapping(value = "/github-code")
    public RedirectView getGitHubToken(String code, String state, HttpServletResponse response)  {
        return  authService.gitHubLogin(code,state,response);
    }



    @GetMapping("/auth/{platform}")
    public RedirectView loginAuth(@PathVariable String platform, @RequestParam(required = false, defaultValue = "/") String redirect, HttpServletRequest request) {
        return authService.loginAuth(platform, redirect, request);

    }











}
