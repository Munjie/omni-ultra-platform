package com.munjie.omni.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.munjie.omni.pojo.entity.UserRoleEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户角色关联表 Mapper 接口
 * </p>
 *
 * @author muwenjie
 * @since 2025-03-21
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRoleEntity> {

}
