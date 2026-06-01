package com.munjie.omni.pojo.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ArticleStatusReq {

    private Integer id;
    private Integer status;
}
