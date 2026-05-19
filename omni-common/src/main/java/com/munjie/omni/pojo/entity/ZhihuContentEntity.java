package com.munjie.omni.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;

/**
 * <p>
 * 知乎内容（回答、文章、视频）
 * </p>
 *
 * @author mwj
 * @since 2025-08-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
@TableName("zhihu_content")
public class ZhihuContentEntity implements Serializable {


    /**
     * 自增ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 内容ID
     */
    @TableField("content_id")
    private String contentId;

    /**
     * 内容类型(article | answer | zvideo)
     */
    @TableField("content_type")
    private String contentType;

    /**
     * 内容文本, 如果是视频类型这里为空
     */
    @TableField("content_text")
    private String contentText;

    /**
     * 内容落地链接
     */
    @TableField("content_url")
    private String contentUrl;

    /**
     * 问题ID, type为answer时有值
     */
    @TableField("question_id")
    private String questionId;

    /**
     * 内容标题
     */
    private String title;

    /**
     * 内容描述
     */
    @TableField("`desc`")
    private String desc;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private String createdTime;

    /**
     * 更新时间
     */
    @TableField("updated_time")
    private String updatedTime;

    /**
     * 赞同人数
     */
    @TableField("voteup_count")
    private Integer voteupCount;

    /**
     * 评论数量
     */
    @TableField("comment_count")
    private Integer commentCount;

    /**
     * 来源关键词
     */
    @TableField("source_keyword")
    private String sourceKeyword;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 用户主页链接
     */
    @TableField("user_link")
    private String userLink;

    /**
     * 用户昵称
     */
    @TableField("user_nickname")
    private String userNickname;

    /**
     * 用户头像地址
     */
    @TableField("user_avatar")
    private String userAvatar;

    /**
     * 用户url_token
     */
    @TableField("user_url_token")
    private String userUrlToken;

    /**
     * 记录添加时间戳
     */
    @TableField("add_ts")
    private Long addTs;

    /**
     * 记录最后修改时间戳
     */
    @TableField("last_modify_ts")
    private Long lastModifyTs;


}
