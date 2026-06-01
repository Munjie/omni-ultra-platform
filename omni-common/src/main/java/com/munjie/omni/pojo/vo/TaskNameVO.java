package com.munjie.omni.pojo.vo;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaskNameVO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer value;
    private String label;


}
