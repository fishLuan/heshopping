package com.youkeda.hashopping.authorization.api;

import com.youkeda.hashopping.authorization.model.RouteResponse;
import com.youkeda.hashopping.authorization.service.UserRouteService;
import com.youkeda.hashopping.common.model.Result;
import com.youkeda.hashopping.security.context.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/** 提供当前登录用户的数据库动态路由。 */
@RestController
public class UserRouteController {
    private final CurrentUser currentUser;
    private final UserRouteService routes;

    /** 注入当前身份入口和路由服务。 */
    public UserRouteController(CurrentUser currentUser, UserRouteService routes) {
        this.currentUser = currentUser;
        this.routes = routes;
    }

    /** 从认证上下文取得用户 ID，避免客户端指定其他用户查询授权。 */
    @GetMapping("/api/user/routes")
    public Result<List<RouteResponse>> routes(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        return Result.success(routes.findRoutes(currentUser.requireUserId()));
    }
}
