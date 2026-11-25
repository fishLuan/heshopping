package com.youkeda.hashopping.authorization.service;

import com.youkeda.hashopping.authorization.dao.MenuDAO;
import com.youkeda.hashopping.authorization.model.RouteResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 过滤有效授权菜单，并按数据库排序组装路由树。 */
@Service
public class UserRouteService {
    private final MenuDAO menus;
    private final AuthorizationService authorization;

    /** 注入菜单查询和授权查询服务。 */
    public UserRouteService(MenuDAO menus, AuthorizationService authorization) {
        this.menus = menus;
        this.authorization = authorization;
    }

    /** 在一致的数据库快照中组装路由，阻止停用父级、缺失父级和循环配置。 */
    @Transactional(readOnly = true)
    public List<RouteResponse> findRoutes(long userId) {
        List<RouteResponse> all = menus.findAll();
        Map<Long, RouteResponse> byId = new HashMap<>();
        for (RouteResponse node : all) {
            byId.put(node.id, node);
        }
        Set<String> permissions = new HashSet<>(authorization.findPermissions(userId));
        Set<Long> included = new HashSet<>();
        for (Long id : menus.findGrantedIds(userId)) {
            Set<Long> path = new HashSet<>();
            RouteResponse node = byId.get(id);
            while (node != null && node.enabled && path.add(node.id)
                    && (node.permissionCode == null || permissions.contains(node.permissionCode))) {
                if (node.parentId == null) {
                    included.addAll(path);
                    break;
                }
                node = byId.get(node.parentId);
            }
        }
        List<RouteResponse> roots = new ArrayList<>();
        for (RouteResponse node : all) {
            if (!included.contains(node.id)) {
                continue;
            }
            if (node.parentId == null) {
                roots.add(node);
            } else {
                byId.get(node.parentId).children.add(node);
            }
        }
        return roots;
    }
}
