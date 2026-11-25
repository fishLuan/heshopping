package com.youkeda.hashopping.user.service.impl;

import com.youkeda.hashopping.user.dao.UserDAO;
import com.youkeda.hashopping.authorization.service.AuthorizationService;
import org.springframework.transaction.annotation.Transactional;
import com.youkeda.hashopping.user.dataobject.UserDO;
import com.youkeda.hashopping.common.model.Result;
import com.youkeda.hashopping.user.model.User;
import com.youkeda.hashopping.user.service.UserService;
import com.youkeda.hashopping.user.service.LoginAttemptGuard;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 处理用户注册、密码验证和用户资料查询。 */
@Component
/**
 * 用户登录、身份认证与用户管理：UserServiceImpl。
 */
public class UserServiceImpl implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private AuthorizationService userRoleDAO;

    @Autowired
    private LoginAttemptGuard loginAttemptGuard;

    /** 注册普通用户并保存 BCrypt 哈希。 */
    @Override
    @Transactional
    public Result<User> register(String userName, String pwd) {
        Result<User> result = new Result<>();

        if (StringUtils.isEmpty(userName)) {
            result.setCode("600");
            result.setMessage("用户名不能为空");
            return result;
        }
        if (StringUtils.isEmpty(pwd)) {
            result.setCode("601");
            result.setMessage("密码不能为空");
            return result;
        }

        if (pwd.getBytes(StandardCharsets.UTF_8).length > 72) {
            return Result.error("INVALID_ARGUMENT", "密码不能超过 72 字节");
        }

        UserDO userDO = userDAO.findByUserName(userName);
        if (userDO != null) {
            result.setCode("602");
            result.setMessage("用户名已经存在");
            return result;
        }

        UserDO userDO1 = new UserDO();
        userDO1.setUserName(userName);
        // 初始昵称等于用户名
        userDO1.setNickName(userName);
        userDO1.setPwd(passwordEncoder.encode(pwd));

        userDAO.add(userDO1);
        if (userRoleDAO.assignDefaultRole(userDO1.getId()) != 1) {
            throw new IllegalStateException("默认顾客角色不可用");
        }

        result.setSuccess(true);

        result.setData(userDO1.toModel());

        return result;
    }

    /** 校验密码，并在成功登录后升级旧版哈希。 */
    @Override
    public Result<User> login(String userName, String pwd) {

        Result<User> result = new Result<>();

        if (StringUtils.isEmpty(userName)) {
            result.setCode("600");
            result.setMessage("用户名不能为空");
            return result;
        }
        if (StringUtils.isEmpty(pwd)) {
            result.setCode("601");
            result.setMessage("密码不能为空");
            return result;
        }

        if (pwd.getBytes(StandardCharsets.UTF_8).length > 72) {
            return Result.error("INVALID_ARGUMENT", "密码不能超过 72 字节");
        }

        if (loginAttemptGuard.isLocked(userName)) {
            return lockedResult();
        }

        UserDO userDO = userDAO.findByUserName(userName);
        String storedPassword = userDO == null ? null : userDO.getPwd();
        if (storedPassword == null || !passwordEncoder.matches(pwd, storedPassword)) {
            if (loginAttemptGuard.recordFailure(userName)) {
                return lockedResult();
            }
            return Result.error("INVALID_CREDENTIALS", "用户名或密码错误");
        }

        if (!loginAttemptGuard.clearAfterSuccess(userName)) {
            return lockedResult();
        }

        LocalDateTime loginTime = LocalDateTime.now();
        if (userDAO.recordLogin(userDO.getId(), loginTime) == 0) {
            result.setCode("604");
            result.setMessage("账号不可用");
            return result;
        }

        userDO.setLastLoginAt(loginTime);
        result.setSuccess(true);

        result.setData(userDO.toModel());

        return result;
    }

    /** 返回登录失败次数达到上限的提示。 */
    private Result<User> lockedResult() {
        return Result.warning("LOGIN_LOCKED", "登录失败次数过多，请在锁定解除后重试（最长 30 分钟）");
    }

    @Override
    public List<User> queryUser(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return null;
        }
        List<UserDO> userDOS = userDAO.findByIds(userIds);
        List<User> users = new ArrayList<>();
        if (!CollectionUtils.isEmpty(userDOS)) {
            for (UserDO userDO : userDOS) {
                users.add(userDO.toModel());
            }
        }
        return users;
    }
}
