package com.munjie.omni.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.munjie.omni.mapper.SysRoleMapper;
import com.munjie.omni.pojo.entity.SysRoleEntity;
import com.munjie.omni.service.SysRoleService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author muwenjie
 * @since 2023-12-25
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleService {

    @Override
    public String getRowNameById(Integer id) {
        SysRoleEntity entity = this.getById(id);
        return entity.getRoleName();
    }
}
