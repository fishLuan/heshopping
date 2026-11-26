package com.youkeda.hashopping.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youkeda.hashopping.security.filter.SessionAuthenticationFilter;
import com.youkeda.hashopping.common.model.Result;
import com.youkeda.hashopping.authorization.service.AuthorizationService;
import com.youkeda.hashopping.user.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 将现有登录会话接入 Spring Security，并根据数据库角色授权。 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserService users;
    private final AuthorizationService roles;
    private final ObjectMapper json;

    /** 注入用户服务、角色查询和统一响应序列化器。 */
    public SecurityConfig(UserService users, AuthorizationService roles, ObjectMapper json) {
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
        http.addFilterBefore(new SessionAuthenticationFilter(users, roles), AnonymousAuthenticationFilter.class);
        http.authorizeRequests()
                .antMatchers("/user/del", "/user/batchAdd").denyAll()
                .antMatchers(HttpMethod.POST, "/user").denyAll()
                .antMatchers("/users", "/user", "/user/**", "/api/order/queryrecentpaysuccess").hasAuthority("ADMIN")
                .antMatchers("/api/user/me", "/api/user/routes", "/api/order/**", "/api/alipay/**", "/api/payment/mock/**", "/api/addresses", "/api/addresses/**", "/api/cart", "/api/cart/**").authenticated()
                .antMatchers("/api/user/login", "/api/user/reg", "/api/user/me", "/api/user/logout",
                        "/api/product/**", "/api/productdetail/**", "/controller/pay/**",
                        "/", "/index.html", "/assets/**", "/error").permitAll()
                .anyRequest().denyAll();
    }

    /** 使用前端统一格式返回认证或授权失败。 */
    private void reject(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        json.writeValue(response.getWriter(), Result.error(code, message));
    }
}

