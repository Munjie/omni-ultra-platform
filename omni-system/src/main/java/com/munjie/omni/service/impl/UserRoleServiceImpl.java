package com.munjie.omni.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.munjie.omni.mapper.UserRoleMapper;
import com.munjie.omni.pojo.entity.UserRoleEntity;
import com.munjie.omni.service.UserRoleService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户角色关联表 服务实现类
 * </p>
 *
 * @author muwenjie
 * @since 2025-03-21
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRoleEntity> implements UserRoleService {

}
