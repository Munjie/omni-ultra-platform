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
 * @since 2024-06-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
@TableName("txt_info")
public class TxtInfoEntity implements Serializable {


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("content")
    private String content;

    @TableField("key_name")
    private String keyName;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;


}
