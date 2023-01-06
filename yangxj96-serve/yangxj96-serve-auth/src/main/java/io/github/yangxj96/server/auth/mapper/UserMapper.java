/*****************************
 * Copyright (c) 2021 - 2023
 * author:yangxj96
 * email :yangxj96@gmail.com
 * date  :2023-01-07 00:08:39
 * Copyright (c) 2021 - 2023
 ****************************/

package io.github.yangxj96.server.auth.mapper;

import io.github.yangxj96.bean.user.Authority;
import io.github.yangxj96.bean.user.Role;
import io.github.yangxj96.bean.user.User;
import io.github.yangxj96.common.base.BasicMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户mapper层
 *
 * @author yangxj96
 * @version 1.0
 * @date 2023-01-07 00:14
 */
public interface UserMapper extends BasicMapper<User> {

    boolean relevance(@Param("id") Long id, @Param("user") Long user, @Param("role") Long role);

    /**
     * 根据用户id查询角色列表
     *
     * @param userId 用户id
     * @return 角色列表
     */
    List<Role> getRoleByUserId(@Param("userId") Long userId);

    /**
     * 根据角色列表查询用权限列表
     *
     * @param roleIds 用户列表
     * @return 权限列表
     */
    List<Authority> getAuthorityByRoleIds(@Param("ids") List<Long> roleIds);
}