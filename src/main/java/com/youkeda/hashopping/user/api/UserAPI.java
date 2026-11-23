package com.youkeda.hashopping.user.api;

import com.youkeda.hashopping.user.model.LoginUserResponse;
import com.youkeda.hashopping.user.dao.UserRoleDAO;
import com.youkeda.hashopping.common.model.Result;
import com.youkeda.hashopping.user.model.User;
import com.youkeda.hashopping.user.model.UserStatus;
import com.youkeda.hashopping.user.param.LoginRequest;
import com.youkeda.hashopping.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;

/** 提供注册、登录、当前用户查询和退出接口。 */
@RestController
/**
 * 用户登录、身份认证与用户管理：UserAPI。
 */
public class UserAPI {
    private static final String SESSION_USER_ID = "userId";

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleDAO userRoleDAO;

    /** 使用用户名和密码注册普通用户。 */
    @PostMapping("/api/user/reg")
    public Result<User> reg(@RequestParam("userName") String userName, @RequestParam("pwd") String pwd) {
        return userService.register(userName, pwd);
    }

    /** 接收前端发送的 JSON 登录请求。 */
    @PostMapping(value = "/api/user/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<LoginUserResponse> login(@RequestBody(required = false) LoginRequest credentials,
                                          HttpServletRequest request, HttpServletResponse response) {
        return authenticate(credentials == null ? null : credentials.getUserName(),
                credentials == null ? null : credentials.getPassword(), request, response);
    }

    /** 使用现有的账号校验逻辑，验证成功后轮换会话 ID 并保存用户 ID。 */
    private Result<LoginUserResponse> authenticate(String userName, String password,
                                                   HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        if (userName == null || userName.isBlank() || password == null || password.isBlank()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return Result.error("INVALID_ARGUMENT", "请输入用户名和密码");
        }
        Result<User> result = userService.login(userName, password);
        if (!result.isSuccess()) {
            if ("LOGIN_LOCKED".equals(result.getCode())) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                return Result.warning(result.getCode(), result.getMessage());
            }
            if ("INVALID_ARGUMENT".equals(result.getCode())) {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return Result.error(result.getCode(), result.getMessage());
            }
            if ("604".equals(result.getCode())) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                return Result.error("ACCOUNT_DISABLED", "账号不可用");
            }
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return Result.error("INVALID_CREDENTIALS", "用户名或密码错误");
        }

        HttpSession session = request.getSession();
        request.changeSessionId();
        session.setAttribute(SESSION_USER_ID, result.getData().getId());
        return Result.success("OK", "登录成功", new LoginUserResponse(result.getData(), userRoleDAO.findEnabledCodes(result.getData().getId())));
    }

    /** 从现有会话读取登录用户，并重新检查账号是否可用。 */
    @GetMapping("/api/user/me")
    public Result<LoginUserResponse> me(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        HttpSession session = request.getSession(false);
        Object userId = session == null ? null : session.getAttribute(SESSION_USER_ID);
        if (!(userId instanceof Long)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return Result.error("UNAUTHENTICATED", "请先登录");
        }

        List<User> users = userService.queryUser(Collections.singletonList((Long) userId));
        if (users.isEmpty() || users.get(0).getStatus() != UserStatus.ACTIVE) {
            session.invalidate();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return Result.error("UNAUTHENTICATED", "登录已失效，请重新登录");
        }
        return Result.success(new LoginUserResponse(users.get(0), userRoleDAO.findEnabledCodes((Long) userId)));
    }

    /** 销毁当前会话，多次退出也返回成功。 */
    @PostMapping("/api/user/logout")
    public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Result.success("OK", "已退出登录", null);
    }
}
