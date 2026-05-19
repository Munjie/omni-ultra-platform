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
 * @author muwenjie
 * @since 2025-01-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
@TableName("article_tag")
public class ArticleTagEntity implements Serializable {


    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 文章ID
     */
    @TableField("article_id")
    private Integer articleId;

    /**
     * 标签id
     */
    @TableField("tag_id")
    private Integer tagId;






}
