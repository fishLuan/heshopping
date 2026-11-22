package com.youkeda.hashopping.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/** 登录和会话查询返回的用户资料，仅包含允许返回给当前用户的字段。 */
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
    private final UserRole role;
    @JsonProperty
    private final LocalDateTime lastLoginAt;

    /** 从用户模型复制允许返回给前端的字段。 */
    public LoginUserResponse(User user) {
        this.id = user.getId();
        this.userName = user.getUserName();
        this.nickName = user.getNickName();
        this.avatar = user.getAvatar();
        this.role = user.getRole();
        this.lastLoginAt = user.getLastLoginAt();
    }
}
