package com.youkeda.hashopping.authorization.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/** 查询有效角色，并为公开注册的用户分配默认角色。 */
@Mapper
public interface UserRoleDAO {
    /** 查询用户当前启用的全部角色码。 */
    List<String> findEnabledCodes(@Param("userId") long userId);

    /** 通过数据库角色记录为新账号分配顾客权限。 */
    int assignDefaultRole(@Param("userId") long userId);
}

