package com.munjie.omni.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.munjie.omni.pojo.dto.ArticleStatusReq;
import com.munjie.omni.pojo.entity.CommentEntity;
import com.munjie.omni.pojo.vo.CommentVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author mwj
 * @since 2025-12-29
 */
public interface CommentService extends IService<CommentEntity> {

     List<CommentVO> getCommentTree(Integer articleId, Integer currentUserId);

     boolean toggleLike(Integer commentId, Integer userId);

    IPage<CommentEntity> pageAllComment(Integer pageNum, Integer pageSize);

    List<CommentEntity> getLatestComments(int limit);

    int updateStatus(ArticleStatusReq req);

}
