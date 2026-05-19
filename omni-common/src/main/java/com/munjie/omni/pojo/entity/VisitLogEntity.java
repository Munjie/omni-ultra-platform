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
 * @since 2025-01-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
@TableName("visit_log")
public class VisitLogEntity implements Serializable {


    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * ip
     */
    private String ip;

    /**
     * 省份
     */
    private String province;

    @TableField("user_agent")
    private String userAgent;


    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;



}
