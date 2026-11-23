package com.youkeda.hashopping.user.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** 查询有效角色，并为公开注册的用户分配默认角色。 */
@Mapper
public interface UserRoleDAO {
    /** 查询用户当前启用的全部角色码。 */
    @Select("SELECT r.code FROM user_role ur JOIN `role` r ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.enabled = TRUE ORDER BY r.code")
    List<String> findEnabledCodes(@Param("userId") long userId);

    /** 通过数据库角色记录为新账号分配顾客权限。 */
    @Insert("INSERT INTO user_role (user_id, role_id) " +
            "SELECT #{userId}, id FROM `role` WHERE code = 'CUSTOMER' AND enabled = TRUE")
    int assignDefaultRole(@Param("userId") long userId);
}
