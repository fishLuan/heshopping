package com.youkeda.hashopping.authorization.dao;

import com.youkeda.hashopping.authorization.model.RouteResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/** 读取菜单结构及角色授予的菜单入口。 */
@Mapper
public interface MenuDAO {
    /** 查询树结构，包括停用节点，以阻止其子节点被意外下发。 */
    List<RouteResponse> findAll();

    /** 查询有效角色直接授予的菜单主键。 */
    List<Long> findGrantedIds(@Param("userId") long userId);
}
