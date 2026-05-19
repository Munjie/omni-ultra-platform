package com.munjie.omni.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.munjie.omni.exception.CustomException;
import com.munjie.omni.mapper.SysUserEntityMapper;
import com.munjie.omni.pojo.entity.SysUserEntity;
import com.munjie.omni.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author muwenjie
 * @since 2023-12-25
 */
@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserEntityMapper, SysUserEntity> implements SysUserService {



    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserEntity getOrCreateUser(SysUserEntity sysUser) {
        LambdaQueryWrapper<SysUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserEntity::getOpenid, sysUser.getOpenid());
        SysUserEntity user = this.getOne(wrapper);
        if (user != null) {
            return user;
        }
        boolean saved = this.save(sysUser);
        if (!saved) {
            throw new CustomException("用户注册失败");
        }
        return sysUser;
    }
}
