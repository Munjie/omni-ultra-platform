package com.munjie.omni.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 菜单表
 * </p>
 *
 * @author mwj
 * @since 2025-07-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@TableName("menu_item")
public class SysMenuItemEntity implements Serializable {


    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 序号
     */
    @TableField("`index`")
    private String index;



    private String title;

    /**
     * 图标
     */
    private String icon;




    /**
     * 父节点
     */
    private Integer pid;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 子菜单
     */
    @TableField(exist = false)
    private List<SysMenuItemEntity> children;


}
