package com.munjie.omni.pojo.dto;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author 86158
 * @Auther: munjie
 * @Date: 2/19/2021 22:14
 * @Description:
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
public class SysUserReq implements Serializable {

    private static final long serialVersionUID = 1L;


    private Integer id;
    private String userName;
    private String password;
    private String bio;
    private List<Integer> roleIds;




}
