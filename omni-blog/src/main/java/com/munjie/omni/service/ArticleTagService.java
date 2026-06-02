package com.munjie.omni.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.munjie.omni.pojo.entity.ArticleTagEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author muwenjie
 * @since 2025-01-23
 */
public interface ArticleTagService extends IService<ArticleTagEntity> {



    void updateArticleTags(Integer articleId, List<Integer> tagIdList);



}
