package com.munjie.omni.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.munjie.omni.mapper.MenuItemMapper;

import com.munjie.omni.pojo.entity.SysMenuItemEntity;
import com.munjie.omni.pojo.entity.SysPermissionEntity;
import com.munjie.omni.pojo.entity.UserRoleEntity;
import com.munjie.omni.service.SysMenuItemService;
import com.munjie.omni.service.SysPermissionService;
import com.munjie.omni.service.UserRoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author mwj
 * @since 2025-07-30
 */
@Service
public class SysMenuItemServiceImpl extends ServiceImpl<MenuItemMapper, SysMenuItemEntity> implements SysMenuItemService {


    @Resource
    private SysPermissionService permissionService;


    @Resource
    private UserRoleService roleService;


    @Override
    public List<SysMenuItemEntity> listMenuById(Integer userId) {
        // 1. 查询用户的所有角色
        List<UserRoleEntity> userRoles = roleService.lambdaQuery()
                .eq(UserRoleEntity::getUserId, userId)
                .list();
        List<Integer> roleIds = userRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 2. 查询所有角色对应的菜单ID
        List<SysPermissionEntity> permissions = permissionService.lambdaQuery()
                .in(SysPermissionEntity::getRoleId, roleIds)
                .list();
        Set<Integer> menuIds = permissions.stream()
                .map(SysPermissionEntity::getMenuId)
                .collect(Collectors.toSet());

        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 3. 查找所有父节点ID
        List<SysMenuItemEntity> allMenus = this.list();
        Set<Integer> allMenuIds = new HashSet<>(menuIds);
        for (Integer menuId : menuIds) {
            findParentMenuIds(menuId, allMenus, allMenuIds);
        }
        // 4. 构建菜单树
        List<SysMenuItemEntity> filteredMenus = allMenus.stream()
                .filter(menu -> allMenuIds.contains(menu.getId().intValue()))
                .collect(Collectors.toList());
        return buildTree(filteredMenus, 0);

    }

    private void findParentMenuIds(Integer menuId, List<SysMenuItemEntity> allMenus, Set<Integer> allMenuIds) {
        SysMenuItemEntity menu = allMenus.stream()
                .filter(m -> m.getId().intValue() == menuId)
                .findFirst()
                .orElse(null);
        if (menu == null || menu.getPid() == null || menu.getPid() == 0) {
            return;
        }
        allMenuIds.add(menu.getPid());
        findParentMenuIds(menu.getPid(), allMenus, allMenuIds);
    }

    private List<SysMenuItemEntity> buildTree(List<SysMenuItemEntity> menus, Integer parentId) {
        return menus.stream()
                .filter(menu -> (menu.getPid() == null && parentId == 0) || (menu.getPid() != null && menu.getPid().equals(parentId)))
                .peek(menu -> menu.setChildren(buildTree(menus, menu.getId().intValue())))
                .sorted((m1, m2) -> (m1.getSort() == null ? 0 : m1.getSort()) - (m2.getSort() == null ? 0 : m2.getSort()))
                .collect(Collectors.toList());
    }

}
