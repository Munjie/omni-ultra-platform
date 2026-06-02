package com.munjie.omni.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.munjie.omni.mapper.ArticleTagMapper;
import com.munjie.omni.pojo.entity.ArticleTagEntity;
import com.munjie.omni.service.ArticleTagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author muwenjie
 * @since 2025-12-14
 */
@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTagEntity> implements ArticleTagService {


        @Transactional
        @Override
        public void updateArticleTags(Integer articleId, List<Integer> tagIdList) {
            // 1. 删除该文章的所有旧关联
            LambdaQueryWrapper<ArticleTagEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ArticleTagEntity::getArticleId, articleId);
            this.remove(wrapper);
            if (tagIdList != null && !tagIdList.isEmpty()) {
                List<ArticleTagEntity> list = tagIdList.stream()
                        .map(tagId -> {
                            ArticleTagEntity at = new ArticleTagEntity();
                            at.setArticleId(articleId);
                            at.setTagId(tagId);
                            return at;
                        })
                        .collect(Collectors.toList());
                this.saveBatch(list);
            }
        }


}
