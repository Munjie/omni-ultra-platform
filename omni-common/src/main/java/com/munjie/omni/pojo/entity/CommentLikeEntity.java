package com.munjie.omni.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author MWJ
 * @since 2025-12-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
@TableName("t_comment_like")
public class CommentLikeEntity implements Serializable {


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("comment_id")
    private Integer commentId;

    @TableField("user_id")
    private Integer userId;


}
