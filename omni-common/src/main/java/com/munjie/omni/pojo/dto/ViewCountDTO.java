package com.munjie.omni.pojo.dto;

import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ViewCountDTO {
    private Integer  articleId;
    private Integer  userId;

}
