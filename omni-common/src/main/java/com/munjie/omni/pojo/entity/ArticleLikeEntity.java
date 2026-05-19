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
@TableName("t_article_like")
public class ArticleLikeEntity implements Serializable {


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("article_id")
    private Integer articleId;

    @TableField("user_id")
    private Integer userId;


}
