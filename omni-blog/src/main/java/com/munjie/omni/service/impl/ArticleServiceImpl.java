package com.munjie.omni.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.munjie.omni.mapper.ArticleMapper;
import com.munjie.omni.pojo.dto.ArticlePageDTO;
import com.munjie.omni.pojo.dto.ArticleStatusReq;
import com.munjie.omni.pojo.entity.ArticleEntity;
import com.munjie.omni.pojo.entity.ArticleLikeEntity;
import com.munjie.omni.pojo.entity.ArticleTagEntity;
import com.munjie.omni.service.ArticleLikeService;
import com.munjie.omni.service.ArticleService;
import com.munjie.omni.service.ArticleTagService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author muwenjie
 * @since 2025-01-23
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, ArticleEntity> implements ArticleService {

    @Resource
    private ArticleMapper articleMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ArticleTagService articleTagService;


    @Resource
    private ArticleLikeService articleLikeService;


    private static final String VIEW_RECORD_KEY = "post:view:record:%d:%s"; // 记录用户
    private static final String VIEW_COUNT_KEY = "post:view:count:%d";     // 记录总量


    @Override
    public IPage<ArticleEntity> pageHomeArticle(ArticlePageDTO pageDTO) {
        LambdaQueryWrapper<ArticleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ArticleEntity::getCreateTime);
        wrapper.eq(ArticleEntity::getStatus,1);
        wrapper.like(StrUtil.isNotBlank(pageDTO.getContent()),ArticleEntity::getTitle,pageDTO.getContent());
        Page<ArticleEntity> pageParam = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        return this.page(pageParam, wrapper);
    }

    @Override
    public IPage<ArticleEntity> pageAllArticle(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ArticleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ArticleEntity::getCreateTime);
        Page<ArticleEntity> pageParam = new Page<>(pageNum, pageSize);
        return this.page(pageParam, wrapper);
    }

    @Override
    public int updateStatus(ArticleStatusReq req) {
        UpdateWrapper<ArticleEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", req.getId())
                .set("status", req.getStatus());
         update(updateWrapper);
         return req.getStatus();
    }

    @Transactional
    public boolean toggleLike(Integer articleId, Integer userId) {
        // 查询点赞记录是否存在
        boolean exists = checkArticleLike(articleId, userId);
        if (exists) {
            articleMapper.deleteArticleLike(articleId, userId);
            return false;
        } else {
            articleMapper.insertArticleLike(articleId, userId);
            return true;
        }
    }

    @Override
    public boolean checkArticleLike(Integer articleId, Integer userId) {
        return   articleMapper.checkArticleLike(articleId, userId) > 0;
    }



    @Override
    public String incrementViewCount(Integer articleId, String identifier) {
        String recordKey = String.format(VIEW_RECORD_KEY, articleId, identifier);
        String countKey = String.format(VIEW_COUNT_KEY, articleId);
        Boolean isFirstView = stringRedisTemplate.opsForValue()
                .setIfAbsent(recordKey, "1", 10, TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(isFirstView)) {
            stringRedisTemplate.opsForValue().increment(countKey);
        }
        return "ok";
    }

    @Scheduled(initialDelay = 0, fixedRate = 3600000)
    public void syncToDatabase() {
        Set<String> keys = stringRedisTemplate.keys("post:view:count:*");
        for (String key : keys) {
            Integer postId = Integer.valueOf(key.substring(key.lastIndexOf(":") + 1));
            String countStr = stringRedisTemplate.opsForValue().get(key);
            int count = Integer.parseInt(countStr);
            articleMapper.updateViews(postId, count);
            stringRedisTemplate.delete(key);
        }
    }

    @Override
    public ArticleEntity getArticleDetail(int id, int userId) {
        ArticleEntity article = this.getOne(new LambdaQueryWrapper<ArticleEntity>().eq(ArticleEntity::getId, id).eq(ArticleEntity::getStatus,1));
        if (article == null) {
            return null;
        }
        CompletableFuture<List<ArticleTagEntity>> tagsFuture = CompletableFuture.supplyAsync(() ->
                articleTagService.list(new LambdaQueryWrapper<ArticleTagEntity>().eq(ArticleTagEntity::getArticleId, id))
        );
        CompletableFuture<List<ArticleLikeEntity>> likesFuture = CompletableFuture.supplyAsync(() ->
                articleLikeService.list(new LambdaQueryWrapper<ArticleLikeEntity>().eq(ArticleLikeEntity::getArticleId, id))
        );
        CompletableFuture<Boolean> likedFuture = CompletableFuture.supplyAsync(() ->
                this.checkArticleLike(id, userId)
        );
        CompletableFuture.allOf(tagsFuture, likesFuture, likedFuture).join();
        try {
            List<ArticleTagEntity> articleTagEntities = tagsFuture.get();
            articleTagEntities.stream().map(ArticleTagEntity::getArticleId);
            article.setTags(article.getTagList());
            article.setLikes(likesFuture.get().size());
            article.setPostLiked(likedFuture.get());
        } catch (Exception e) {
            throw new RuntimeException("查询文章失败", e);
        }
        return article;
    }

    @Override
    public ArticleEntity getArticleDetail(int id) {
      return this.getOne(new LambdaQueryWrapper<ArticleEntity>().eq(ArticleEntity::getId, id));
    }
}
