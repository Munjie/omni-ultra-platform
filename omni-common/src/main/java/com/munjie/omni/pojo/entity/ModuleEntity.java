package com.munjie.omni.pojo.entity;

import lombok.*;

import java.io.Serializable;
import java.util.List;


/**
 * @author muwen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
public class ModuleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    /**
     * 地址
     */
    private String url;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     */
    private String type;

    /**
     * 图片壁纸
     */
    private String imgUrl;

    /**
     * 排序
     */
    private Integer sort;
    /**
     * 模块id
     */
    private String moduleId;

    private List<ModuleEntity> children;

}
