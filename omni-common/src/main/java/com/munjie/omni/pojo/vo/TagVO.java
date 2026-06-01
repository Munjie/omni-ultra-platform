package com.munjie.omni.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class TagVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String label;
    private String type;



}
