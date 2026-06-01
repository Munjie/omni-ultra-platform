package com.munjie.omni.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserSaveReq {
    private Integer id;
    private String userName;
    private String password;
    private String avatar;
    private List<Integer> roleIds;
}