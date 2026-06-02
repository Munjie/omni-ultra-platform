package com.munjie.omni.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.munjie.omni.pojo.dto.ArticlePageDTO;
import com.munjie.omni.pojo.dto.ArticleStatusReq;
import com.munjie.omni.pojo.entity.ArticleEntity;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author muwenjie
 * @since 2025-01-23
 */
public interface ArticleService extends IService<ArticleEntity> {

    IPage<ArticleEntity> pageHomeArticle(ArticlePageDTO articlePageDTO);
    IPage<ArticleEntity> pageAllArticle(Integer pageNum, Integer pageSize);

    int updateStatus(ArticleStatusReq req);

    boolean toggleLike(Integer articleId, Integer userId);

    boolean checkArticleLike(Integer articleId, Integer userId);

    String incrementViewCount(Integer articleId, String identifier);


     ArticleEntity getArticleDetail(int id, int userId);



    ArticleEntity getArticleDetail(int id);

}
