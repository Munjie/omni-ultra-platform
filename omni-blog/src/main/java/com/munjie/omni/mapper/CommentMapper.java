package com.munjie.omni.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.munjie.omni.pojo.entity.CommentEntity;
import com.munjie.omni.pojo.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author mwj
 * @since 2025-12-29
 */
@Mapper
public interface CommentMapper extends BaseMapper<CommentEntity> {



    /**
     * 查询文章下所有评论（基础数据）
     */
    List<CommentVO> selectCommentVOList(@Param("articleId") Integer articleId);

    /**
     * 查询用户点赞过的评论ID集合
     */
    Set<Integer> getLikedCommentIdsByUser(@Param("articleId") Integer articleId, @Param("userId") Integer userId);

    /**
     * 检查是否已点赞某评论
     */
    Integer checkCommentLike(@Param("commentId") Integer commentId, @Param("userId") Integer userId);

    /**
     * 写入点赞记录
     */
    int insertCommentLike(@Param("commentId") Integer commentId, @Param("userId") Integer userId);

    /**
     * 删除点赞记录
     */
    int deleteCommentLike(@Param("commentId") Integer commentId, @Param("userId") Integer userId);

    /**
     * 增减评论表的点赞统计字段
     */
    void updateLikeCount(@Param("commentId") Integer commentId, @Param("offset") Integer offset);

    @Select("SELECT COUNT(*) FROM t_comment WHERE DATE(create_time) = CURDATE()")
    long getTodayNewComments();
}