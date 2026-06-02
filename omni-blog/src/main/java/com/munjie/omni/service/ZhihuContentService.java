package com.munjie.omni.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.munjie.omni.pojo.entity.ZhihuContentEntity;


/**
 * <p>
 * 知乎内容（回答、文章、视频） 服务类
 * </p>
 *
 * @author mwj
 * @since 2025-08-04
 */
public interface ZhihuContentService extends IService<ZhihuContentEntity> {

     void syncData();

}
