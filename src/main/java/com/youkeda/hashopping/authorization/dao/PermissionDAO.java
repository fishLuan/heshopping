package com.youkeda.hashopping.authorization.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/** 查询用户通过有效角色获得的操作权限。 */
@Mapper
public interface PermissionDAO {
    /** 返回去重且启用的权限码。 */
    @Select("SELECT DISTINCT p.code FROM user_role ur JOIN role r ON r.id=ur.role_id " +
            "JOIN role_permission rp ON rp.role_id=r.id JOIN permission p ON p.id=rp.permission_id " +
            "WHERE ur.user_id=#{userId} AND r.enabled=TRUE AND p.enabled=TRUE ORDER BY p.code")
    List<String> findEnabledCodes(@Param("userId") long userId);
}
