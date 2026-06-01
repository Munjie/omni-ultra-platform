package com.munjie.omni.pojo.vo;

import com.munjie.omni.enums.ArticleStatusEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class ArticleVO implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 发布时间
     */
    private String publishTime;

    /**
     * 阅读量
     */
    private Integer views;

    /**
     * 是否发布
     */
    private ArticleStatusEnum status;

    /**
     * 文章分类
     */
    private String category;
}
