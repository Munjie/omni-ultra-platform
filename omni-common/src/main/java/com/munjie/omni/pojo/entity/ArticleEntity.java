package com.munjie.omni.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
@TableName("article")
public class ArticleEntity implements Serializable {


    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 文章图片
     */
    private String image;

    /**
     * 文章介绍
     */
    private String introduction;

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
     * 阅读量
     */
    private Integer views;

    /**
     * 是否发布1=已发布，0=未发布
     */
//    private ArticleStatusEnum status;
    private Integer status;

    /**
     * 分类
     */
    private String category;

    private String tag;

    @TableField(exist = false)
    private List<String> tags;

    @TableField(exist = false)
    private Integer likes;

    @TableField(exist = false)
    private boolean postLiked;


    public List<String> getTagList() {
        if (tag == null || tag.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tag.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }


    public void setTagList(List<String> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            this.tag = null;
        } else {
            this.tag = tagList.stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(","));
        }
    }

}
