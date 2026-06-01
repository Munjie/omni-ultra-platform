package com.munjie.omni.pojo.dto;

import lombok.*;

/**
 * @Description: TODO
 * @author: Munjie
 * @date: 2024/10/27日 21:13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ArticlePageDTO {

    private String  content;
    private Integer pageSize;
    private Integer pageNum;
}
