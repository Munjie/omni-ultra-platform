package com.munjie.omni.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.munjie.omni.mapper.ZhihuContentMapper;
import com.munjie.omni.pojo.entity.ArticleEntity;
import com.munjie.omni.pojo.entity.ZhihuContentEntity;
import com.munjie.omni.service.ArticleService;
import com.munjie.omni.service.ZhihuContentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 知乎内容（回答、文章、视频） 服务实现类
 * </p>
 *
 * @author mwj
 * @since 2025-08-04
 */
@Service
public class ZhihuContentServiceImpl extends ServiceImpl<ZhihuContentMapper, ZhihuContentEntity> implements ZhihuContentService {

    @Resource
    private ArticleService articleService;

    @Override
    public void syncData(){
        List<ZhihuContentEntity> list = this.list();
        List<ArticleEntity> collect = list.stream().map(m -> {
            return ArticleEntity.builder().title(m.getTitle()).introduction(m.getDesc()).content(m.getContentText()).build();
        }).toList();
        articleService.saveBatch(collect);
    }

}
