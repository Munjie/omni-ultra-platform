package com.munjie.omni.pojo.vo;

import lombok.*;

import java.util.List;

/**
 * 班级总分详情 vo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClassScore {
    
    private List<String> categories;
    private List<Double> value;


}