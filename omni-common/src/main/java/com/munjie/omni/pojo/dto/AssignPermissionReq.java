package com.munjie.omni.pojo.dto;// 1. DTO 类（用于接收前端请求参数）

import lombok.Data;

import java.util.List;

@Data
public class AssignPermissionReq {

    private Integer roleId;
    private List<Integer> menuIds;
}