package com.youkeda.hashopping.authorization.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/** 查询用户通过有效角色获得的操作权限。 */
@Mapper
public interface PermissionDAO {
    /** 返回去重且启用的权限码。 */
    List<String> findEnabledCodes(@Param("userId") long userId);
}
