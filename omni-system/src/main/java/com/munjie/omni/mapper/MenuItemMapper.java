package com.munjie.omni.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.munjie.omni.pojo.entity.SysMenuItemEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 菜单表 Mapper 接口
 * </p>
 *
 * @author mwj
 * @since 2025-07-30
 */
@Mapper
public interface MenuItemMapper extends BaseMapper<SysMenuItemEntity> {

}
