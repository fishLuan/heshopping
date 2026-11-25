package com.youkeda.hashopping.authorization.dao;

import com.youkeda.hashopping.authorization.model.RouteResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/** 读取菜单结构及角色授予的菜单入口。 */
@Mapper
public interface MenuDAO {
    /** 查询树结构，包括停用节点，以阻止其子节点被意外下发。 */
    @Select("SELECT id,parent_id AS parentId,name,node_type AS nodeType,route_name AS routeName,path," +
            "component_key AS componentKey,permission_code AS permissionCode,icon,sort_order AS sortOrder," +
            "visible,enabled FROM menu ORDER BY sort_order,id")
    List<RouteResponse> findAll();

    /** 查询有效角色直接授予的菜单主键。 */
    @Select("SELECT DISTINCT rm.menu_id FROM user_role ur JOIN role r ON r.id=ur.role_id " +
            "JOIN role_menu rm ON rm.role_id=r.id WHERE ur.user_id=#{userId} AND r.enabled=TRUE")
    List<Long> findGrantedIds(@Param("userId") long userId);
}
