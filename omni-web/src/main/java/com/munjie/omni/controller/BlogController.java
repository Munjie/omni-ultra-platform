package com.munjie.omni.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.munjie.omni.annotation.RateLimit;
import com.munjie.omni.mapper.TagMapper;
import com.munjie.omni.pojo.dto.ArticlePageDTO;
import com.munjie.omni.pojo.dto.ViewCountDTO;
import com.munjie.omni.pojo.entity.*;
import com.munjie.omni.pojo.vo.ArticleLikeStatusVO;
import com.munjie.omni.pojo.vo.CommentVO;
import com.munjie.omni.service.*;
import com.munjie.omni.utils.IpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/article")
@Tag(name = "文章管理API")
@Slf4j
public class BlogController {

    @Resource
    private ArticleService articleService;

    @Resource
    private TagMapper tagMapper;

    @Resource
    private CommentService commentService;

    @Resource
    private ArticleLikeService articleLikeService;

    @Resource
    private CommentLikeService commentLikeService;




    @PostMapping("/page-home-article")
    @Operation(summary ="分页查询主页文章")
    @RateLimit(limit = 100, duration = 60)
    public IPage<ArticleEntity> pageHomeArticle(@RequestBody ArticlePageDTO articlePageDTO, HttpServletRequest request) {
        return articleService.pageHomeArticle(articlePageDTO);
    }



    @Operation(summary ="文章详细信息")
    @GetMapping("/get-article-by-id/{id}/{userId}")
    @RateLimit(limit = 100, duration = 60)
    public ArticleEntity getArticleById(@PathVariable("id") int id,@PathVariable("userId") int userId) {
        return articleService.getArticleDetail(id,userId);
    }



    @GetMapping("/all-tags")
    @Operation(summary ="所有标签")
    public List<TagEntity> allTags() {
      return tagMapper.selectList(new LambdaQueryWrapper<TagEntity>().orderByAsc(TagEntity::getName).eq(TagEntity::getType,"tag"));
    }


    @GetMapping("/all-category")
    @Operation(summary ="所有分类")
    public List<TagEntity> allCategory() {
        return tagMapper.selectList(new LambdaQueryWrapper<TagEntity>().orderByAsc(TagEntity::getName).eq(TagEntity::getType,"category"));
    }



    // 1. 获取文章评论列表 (嵌套结构)
    @GetMapping("/all-comment/{articleId}/{userId}")
    public  List<CommentVO> getComments(@PathVariable Integer articleId, @PathVariable Integer userId) {
        return commentService.getCommentTree(articleId, userId);
    }

    // 2. 发表评论/回复
    @PostMapping("/add-comment")
    @RateLimit(limit = 100, duration = 60)
    public String addComment(@RequestBody CommentEntity comment) {
        commentService.save(comment);
        return "发布成功";
    }

    // 3. 评论点赞 (切换状态)
    @GetMapping("/comment-like/{commentId}/{userId}")
    @RateLimit(limit = 100, duration = 60)
    public ArticleLikeStatusVO likeComment(@PathVariable Integer commentId, @PathVariable Integer userId) {
        boolean b = commentService.toggleLike(commentId, userId);
        List<CommentLikeEntity> likeEntities = commentLikeService.list(new LambdaQueryWrapper<CommentLikeEntity>().eq(CommentLikeEntity::getCommentId, commentId));
        return ArticleLikeStatusVO.builder().liked(b).likeCount(likeEntities.size()).build();
    }

    // 4. 文章点赞 (切换状态)
    @GetMapping("/article-like/{articleId}/{userId}")
    @RateLimit(limit = 100, duration = 60)
    public ArticleLikeStatusVO likeArticle(@PathVariable Integer articleId,@PathVariable Integer userId) {
        boolean b = articleService.toggleLike(articleId, userId);
        List<ArticleLikeEntity> likeEntities = articleLikeService.list(new LambdaQueryWrapper<ArticleLikeEntity>().eq(ArticleLikeEntity::getArticleId, articleId));
        return ArticleLikeStatusVO.builder().liked(b).likeCount(likeEntities.size()).build();
    }


    @Operation(summary ="view")
    @PostMapping("/count-view")
    public String countView(@RequestBody ViewCountDTO countDTO, HttpServletRequest request) {
        String ip = IpUtil.getRealIp(request);
        String ua = request.getHeader("User-Agent");
        Integer userId = countDTO.getUserId();
        String identifier = DigestUtils.md5DigestAsHex((ip + ua + userId).getBytes());
        return articleService.incrementViewCount(countDTO.getArticleId(), identifier);


    }








}
