package com.youkeda.hashopping.authorization.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

/** 动态路由节点，隐藏菜单仍会作为可访问路由返回。 */
public class RouteResponse {
    public Long id;
    @JsonIgnore
    public Long parentId;
    public String name;
    public String nodeType;
    public String routeName;
    public String path;
    public String componentKey;
    public String permissionCode;
    public String icon;
    public int sortOrder;
    public boolean visible;
    @JsonIgnore
    public boolean enabled;
    public List<RouteResponse> children = new ArrayList<>();
}
