package com.munjie.omni.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.munjie.omni.pojo.entity.SysRoleEntity;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author muwenjie
 * @since 2023-12-25
 */
public interface SysRoleService extends IService<SysRoleEntity> {

     String getRowNameById(Integer id);

}
