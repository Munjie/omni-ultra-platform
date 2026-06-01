package com.munjie.omni.pojo.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PermissionSaveReq {
    private Integer roleId;
    private String roleName;
    private List<Integer> menuIds;


}