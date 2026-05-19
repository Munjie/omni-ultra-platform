package com.munjie.omni.controller;

import com.munjie.omni.pojo.entity.SysMenuItemEntity;
import com.munjie.omni.service.SysMenuItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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







    @GetMapping("/list-menu/{userId}")
    @Operation(summary ="查询用户菜单")
    public List<SysMenuItemEntity> listMenuById(@PathVariable("userId") Integer userId) {
        return menuItemService.listMenuById(userId);
    }










}
