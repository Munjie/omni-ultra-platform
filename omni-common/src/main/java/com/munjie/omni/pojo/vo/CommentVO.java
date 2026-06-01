package com.munjie.omni.pojo.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

// CommentVO.java
@Data
public class CommentVO {
    private Integer id;
    private Integer userId;
    private Integer parentId;
    private String username; // 关联查询获取
    private String avatar;
    private String content;
    private Integer likes;
    private Date createTime;
    private Boolean isLiked; // 当前登录用户是否已点赞
    private List<CommentVO> children; // 子评论列表
}