package com.youkeda.hashopping.user.service;

import com.youkeda.hashopping.user.dao.UserDAO;
import com.youkeda.hashopping.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 为下单事务提供买家状态检查和同买家并发锁。 */
@Service
public class BuyerService {
    private final UserDAO users;
    /** 注入用户持久层。 */
    public BuyerService(UserDAO users) { this.users = users; }
    /** 锁定有效买家直到订单事务结束。 */
    @Transactional(propagation = Propagation.MANDATORY)
    public void lockActive(long id) {
        if (users.lockActiveBuyer(id) == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "账号不可用");
        }
    }
}
