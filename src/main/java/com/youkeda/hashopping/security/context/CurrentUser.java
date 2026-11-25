package com.youkeda.hashopping.security.context;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** 为业务代码提供当前请求的身份，避免业务模块直接读取 Session。 */
@Component
public class CurrentUser {
    /** 获取经过会话认证的用户主键，未登录时交由安全处理器返回 401。 */
    public Long requireUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Long)) {
            throw new AuthenticationCredentialsNotFoundException("请先登录");
        }
        return (Long) authentication.getPrincipal();
    }
}
