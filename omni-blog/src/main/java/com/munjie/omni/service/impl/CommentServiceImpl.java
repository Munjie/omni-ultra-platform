package com.munjie.omni.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.munjie.omni.mapper.CommentMapper;
import com.munjie.omni.pojo.dto.ArticleStatusReq;
import com.munjie.omni.pojo.entity.CommentEntity;
import com.munjie.omni.pojo.vo.CommentVO;
import com.munjie.omni.service.CommentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author mwj
 * @since 2025-12-29
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, CommentEntity> implements CommentService {

    @Resource
    private CommentMapper commentMapper;


    @Override
    public List<CommentVO> getCommentTree(Integer articleId, Integer currentUserId) {
        // 1. 查询该文章下所有评论
        List<CommentVO> allComments = commentMapper.selectCommentVOList(articleId);
        Set<Integer> likedCommentIds = new HashSet<>();
        if (currentUserId != null && currentUserId != 0) {
            likedCommentIds = baseMapper.getLikedCommentIdsByUser(articleId, currentUserId);
        }
        return buildCommentTree(allComments,likedCommentIds);
    }


    public List<CommentVO> buildCommentTree(List<CommentVO> allComments,Set<Integer> likedCommentIds) {
        Map<Integer, CommentVO> map = new HashMap<>();
        List<CommentVO> rootList = new ArrayList<>();

        for (CommentVO vo : allComments) {
            vo.setIsLiked(likedCommentIds.contains(vo.getId()));
            map.put(vo.getId(), vo);
            vo.setChildren(new ArrayList<>());

            if (vo.getParentId() == null || vo.getParentId() == 0) {
                rootList.add(vo);
            }
        }

        for (CommentVO vo : allComments) {
            if (vo.getParentId() != null && vo.getParentId() != 0) {
                CommentVO parent = map.get(vo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                }
            }
        }

        // 可对每层children排序（如按createTime或likes）
        sortChildren(rootList);
        return rootList;
    }

    private void sortChildren(List<CommentVO> list) {
        if (list == null) {
            return;
        }
        list.sort(Comparator.comparing(CommentVO::getCreateTime).reversed());
        for (CommentVO vo : list) {
            sortChildren(vo.getChildren());
        }
    }

    @Transactional
    @Override
    public boolean toggleLike(Integer commentId, Integer userId) {
        Integer count = baseMapper.checkCommentLike(commentId, userId);
        if (count > 0) {
            // --- 取消点赞逻辑 ---
            baseMapper.deleteCommentLike(commentId, userId);
            baseMapper.updateLikeCount(commentId, -1);
            return false;
        } else {
            baseMapper.insertCommentLike(commentId, userId);
            baseMapper.updateLikeCount(commentId, 1);
            return true;
        }
    }

    @Override
    public IPage<CommentEntity> pageAllComment(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<CommentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(CommentEntity::getCreateTime);
        Page<CommentEntity> pageParam = new Page<>(pageNum, pageSize);
        return this.page(pageParam, wrapper);
    }

    @Override
    public List<CommentEntity> getLatestComments(int limit) {
        return commentMapper.selectList(new LambdaQueryWrapper<CommentEntity>()
                        .orderByDesc(CommentEntity::getCreateTime)
                        .last("LIMIT " + limit)
        );
    }

    @Override
    public int updateStatus(ArticleStatusReq req) {
        UpdateWrapper<CommentEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", req.getId())
                .set("status", req.getStatus());
        update(updateWrapper);
        return req.getStatus();
    }
}
