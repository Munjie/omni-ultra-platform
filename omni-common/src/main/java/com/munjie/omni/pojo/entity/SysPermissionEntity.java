package com.munjie.omni.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author muwenjie
 * @since 2023-12-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_permission")
public class SysPermissionEntity implements Serializable {


    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 菜单id
     */
    @TableField("menu_id")
    private Integer menuId;

    /**
     * 角色id
     */
    @TableField("role_id")
    private Integer roleId;

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




}
