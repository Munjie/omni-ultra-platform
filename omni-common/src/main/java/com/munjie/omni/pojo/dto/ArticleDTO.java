package com.munjie.omni.pojo.dto;

import lombok.*;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ArticleDTO {
    private Integer  id;
    private String  title;
    private String  content;
    private String introduction;
    private String  articleCover;
    private String  category;
    private List<String> tags;

}
