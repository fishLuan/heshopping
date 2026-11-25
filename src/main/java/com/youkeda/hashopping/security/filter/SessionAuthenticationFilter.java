package com.youkeda.hashopping.security.filter;

import com.youkeda.hashopping.authorization.service.AuthorizationService;
import com.youkeda.hashopping.user.model.User;
import com.youkeda.hashopping.user.model.UserStatus;
import com.youkeda.hashopping.user.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** 从现有会话恢复请求身份，并重新加载有效角色。 */
public class SessionAuthenticationFilter extends OncePerRequestFilter {
    private final UserService users;
    private final AuthorizationService roles;

    /** 注入会话身份校验所需的用户与角色查询服务。 */
    public SessionAuthenticationFilter(UserService users, AuthorizationService roles) {
        this.users = users;
        this.roles = roles;
    }
    /** 登录接口和公开资源沿用原有流程，无需重复查询用户。 */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !(path.equals("/users") || path.equals("/user") || path.startsWith("/user/")
                || (path.equals("/api/user/me") || path.equals("/api/user/routes")) || path.startsWith("/api/order/") || path.startsWith("/api/alipay/"));
    }

    /** 从服务端会话读取用户 ID，并为当前请求创建认证对象。 */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Object id = session == null ? null : session.getAttribute("userId");
        if (id instanceof Long) {
            List<User> matches = users.queryUser(Collections.singletonList((Long) id));
            if (!matches.isEmpty() && matches.get(0).getStatus() == UserStatus.ACTIVE) {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(id, null,
                                roles.findRoles((Long) id).stream()
                                        .map(SimpleGrantedAuthority::new).collect(Collectors.toList())));
            } else {
                session.invalidate();
            }
        }
        chain.doFilter(request, response);
    }

}
