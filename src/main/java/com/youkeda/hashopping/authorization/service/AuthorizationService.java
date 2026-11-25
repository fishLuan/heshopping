package com.youkeda.hashopping.authorization.service;

import com.youkeda.hashopping.authorization.dao.PermissionDAO;
import com.youkeda.hashopping.authorization.dao.UserRoleDAO;
import org.springframework.stereotype.Service;
import java.util.List;

/** 向登录和安全模块提供统一的角色与权限查询入口。 */
@Service
public class AuthorizationService {
    private final UserRoleDAO roles;
    private final PermissionDAO permissions;

    /** 注入授权持久化查询。 */
    public AuthorizationService(UserRoleDAO roles, PermissionDAO permissions) {
        this.roles = roles;
        this.permissions = permissions;
    }

    /** 查询当前有效角色，停用角色不参与授权。 */
    public List<String> findRoles(long userId) {
        return roles.findEnabledCodes(userId);
    }

    /** 查询用户全部有效角色合并后的操作权限。 */
    public List<String> findPermissions(long userId) {
        return permissions.findEnabledCodes(userId);
    }

    /** 注册时分配数据库中的默认顾客角色。 */
    public int assignDefaultRole(long userId) {
        return roles.assignDefaultRole(userId);
    }
}
