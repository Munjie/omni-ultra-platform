package com.munjie.omni.pojo.vo;

import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ArticleLikeStatusVO {
    private boolean liked;
    private Integer likeCount;

}