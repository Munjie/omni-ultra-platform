package com.munjie.omni.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.munjie.omni.pojo.entity.SysMenuItemEntity;

import java.util.List;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author mwj
 * @since 2025-07-30
 */
public interface SysMenuItemService extends IService<SysMenuItemEntity> {

    /**
     * 用户菜单
     * @param userId
     * @return
     */
    List<SysMenuItemEntity> listMenuById(Integer userId);

}
