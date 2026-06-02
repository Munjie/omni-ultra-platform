package com.munjie.omni.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.munjie.omni.pojo.entity.ArticleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author muwenjie
 * @since 2025-01-23
 */
@Mapper
public interface ArticleMapper extends BaseMapper<ArticleEntity> {

    int checkArticleLike(@Param("articleId") Integer articleId, @Param("userId") Integer userId);

    boolean deleteArticleLike(@Param("articleId") Integer articleId, @Param("userId") Integer userId);

    int updateArticleLikeCount(@Param("articleId") Integer articleId, @Param("offset") Integer offset);


    int insertArticleLike(@Param("articleId") Integer articleId, @Param("userId") Integer userId);
    int updateViews(@Param("articleId") Integer articleId, @Param("count") Integer count);


    @Select("SELECT COUNT(*) FROM article WHERE DATE(create_time) = CURDATE() AND status = 1")
    long getTodayNewArticles();


}
