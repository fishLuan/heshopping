package com.youkeda.hashopping.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youkeda.hashopping.common.model.Result;
import com.youkeda.hashopping.user.dao.UserRoleDAO;
import com.youkeda.hashopping.user.model.User;
import com.youkeda.hashopping.user.model.UserStatus;
import com.youkeda.hashopping.user.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
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

/** 将现有登录会话接入 Spring Security，并根据数据库角色授权。 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserService users;
    private final UserRoleDAO roles;
    private final ObjectMapper json;

    /** 注入用户服务、角色查询和统一响应序列化器。 */
    public SecurityConfig(UserService users, UserRoleDAO roles, ObjectMapper json) {
        this.users = users;
        this.roles = roles;
        this.json = json;
    }

    /** 配置公开入口、业务权限、CSRF 和 JSON 错误响应。 */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.requestCache().disable();
        http.logout().disable();
        http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringAntMatchers("/controller/pay/alipaycallback");
        http.exceptionHandling()
                .authenticationEntryPoint((req, res, e) -> reject(res, 401, "UNAUTHENTICATED", "请先登录"))
                .accessDeniedHandler((req, res, e) -> reject(res, 403, "FORBIDDEN", "无权限或请求校验失败"));
        http.addFilterBefore(sessionIdentity(), AnonymousAuthenticationFilter.class);
        http.authorizeRequests()
                .antMatchers("/user/del", "/user/batchAdd").denyAll()
                .antMatchers(HttpMethod.POST, "/user").denyAll()
                .antMatchers("/users", "/user", "/user/**", "/api/order/queryrecentpaysuccess").hasAuthority("ADMIN")
                .antMatchers("/api/order/**", "/api/alipay/**").authenticated()
                .antMatchers("/api/user/login", "/api/user/reg", "/api/user/me", "/api/user/logout",
                        "/api/product/**", "/api/productdetail/**", "/controller/pay/**",
                        "/", "/index.html", "/assets/**", "/error").permitAll()
                .anyRequest().denyAll();
    }

    /** 每次访问业务接口时重新装载状态和角色，使撤权对已有会话生效。 */
    private OncePerRequestFilter sessionIdentity() {
        return new OncePerRequestFilter() {
            /** 登录接口和公开资源沿用原有流程，无需重复查询用户。 */
            @Override
            protected boolean shouldNotFilter(HttpServletRequest request) {
                String path = request.getServletPath();
                return !(path.equals("/users") || path.equals("/user") || path.startsWith("/user/")
                        || path.startsWith("/api/order/") || path.startsWith("/api/alipay/"));
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
                                        roles.findEnabledCodes((Long) id).stream()
                                                .map(SimpleGrantedAuthority::new).collect(Collectors.toList())));
                    } else {
                        session.invalidate();
                    }
                }
                chain.doFilter(request, response);
            }
        };
    }

    /** 使用前端统一格式返回认证或授权失败。 */
    private void reject(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        json.writeValue(response.getWriter(), Result.error(code, message));
    }
}
