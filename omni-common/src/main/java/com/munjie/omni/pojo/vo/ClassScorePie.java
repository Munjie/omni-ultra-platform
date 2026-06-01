package com.munjie.omni.pojo.vo;

import lombok.*;

/**
 * 班级总分详情 vo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClassScorePie {
    
    private String name;
    private Integer value;


}