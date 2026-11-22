package com.youkeda.hashopping.param;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

/** 登录请求，只接收用户名和密码。 */
public class LoginRequest {
    private String userName;

    @JsonAlias("pwd")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /** 获取请求中的用户名。 */
    public String getUserName() {
        return userName;
    }

    /** 设置请求中的用户名。 */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /** 获取用于本次登录验证的密码。 */
    public String getPassword() {
        return password;
    }

    /** 设置用于本次登录验证的密码。 */
    public void setPassword(String password) {
        this.password = password;
    }
}
