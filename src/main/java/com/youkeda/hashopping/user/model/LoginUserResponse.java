package com.youkeda.hashopping.user.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/** 登录和会话查询返回的用户资料，仅包含允许返回给当前用户的字段。 */
/**
 * 用户登录、身份认证与用户管理：LoginUserResponse。
 */
public class LoginUserResponse {
    @JsonProperty
    private final long id;
    @JsonProperty
    private final String userName;
    @JsonProperty
    private final String nickName;
    @JsonProperty
    private final String avatar;
    @JsonProperty
    private final List<String> roles;
    @JsonProperty
    private final LocalDateTime lastLoginAt;

    /** 从用户模型复制允许返回给前端的字段。 */
    public LoginUserResponse(User user, List<String> roles) {
        this.id = user.getId();
        this.userName = user.getUserName();
        this.nickName = user.getNickName();
        this.avatar = user.getAvatar();
        this.roles = List.copyOf(roles);
        this.lastLoginAt = user.getLastLoginAt();
    }
}
