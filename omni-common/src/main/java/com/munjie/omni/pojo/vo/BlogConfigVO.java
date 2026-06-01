package com.munjie.omni.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class BlogConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer articleCount;
    private Integer categoryCount;
    private Integer tagCount;
    private Integer runTime;
    private Integer viewCount;
    private String avatar;


}
