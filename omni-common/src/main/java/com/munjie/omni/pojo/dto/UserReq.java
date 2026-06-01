package com.munjie.omni.pojo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReq {


    private Integer id;


    private String userName;

    private String avatar;


    private String password;
}
