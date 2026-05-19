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
 * @author mwj
 * @since 2025-12-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
@TableName("t_comment")
public class CommentEntity implements Serializable {


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 文章ID
     */
    @TableField("article_id")
    private Integer articleId;

    /**
     * 发表用户ID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 父评论ID (0表示一级评论)
     */
    @TableField("parent_id")
    private Integer parentId;

    @TableField("reply_target_id")
    private Integer replyTargetId;

    /**
     * 评论内容
     */
    private String content;

    private Integer status;

    /**
     * 点赞数
     */
    private Integer likes;

    @TableField("create_time")
    private Date createTime;


}
