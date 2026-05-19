package com.munjie.omni.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.munjie.omni.pojo.entity.SysUserEntity;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author muwenjie
 * @since 2023-12-25
 */
public interface SysUserService extends IService<SysUserEntity> {


     SysUserEntity getOrCreateUser(SysUserEntity sysUser);

}
