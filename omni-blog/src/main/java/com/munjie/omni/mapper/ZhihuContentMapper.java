package com.munjie.omni.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.munjie.omni.pojo.entity.ZhihuContentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 知乎内容（回答、文章、视频） Mapper 接口
 * </p>
 *
 * @author mwj
 * @since 2025-08-04
 */
@Mapper
public interface ZhihuContentMapper extends BaseMapper<ZhihuContentEntity> {

}
