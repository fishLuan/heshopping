package com.youkeda.hashopping.payment.service;

import com.youkeda.hashopping.payment.dao.MockPaymentDAO;
import com.youkeda.hashopping.payment.param.MockPaymentRequest;
import com.youkeda.hashopping.order.model.CreateOrderResponse;
import com.youkeda.hashopping.order.service.OrderPaymentService;
import com.youkeda.hashopping.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/** 开发测试模拟渠道，统一组织订单、销量与支付记录事务。 */
@Service
@Profile({"dev","test"})
public class MockPaymentService {
    private final OrderPaymentService orders;
    private final MockPaymentDAO payments;
    /** 注入订单支付服务与渠道记录持久层。 */
    public MockPaymentService(OrderPaymentService orders, MockPaymentDAO payments) {
        this.orders = orders;
        this.payments = payments;
    }
    /** 模拟成功或失败；重复成功返回已付款订单，不重复写成功记录。 */
    @Transactional
    public CreateOrderResponse pay(long userId, MockPaymentRequest request) {
        if (request == null || request.orderNumber == null || request.orderNumber.isBlank()
                || request.orderNumber.length() > 64 || request.success == null)
            throw new BusinessException(HttpStatus.BAD_REQUEST,"INVALID_ARGUMENT","请输入订单号和模拟结果");
        CreateOrderResponse order = orders.prepare(userId,request.orderNumber);
        if ("TRADE_PAID_SUCCESS".equals(order.status)) return order;
        if (request.success) orders.markPaid(order);
        payments.insert(UUID.randomUUID().toString().replace("-",""),order.userId,order.orderNumber,
                order.totalPrice,request.success ? "SUCCESS" : "FAILURE");
        return order;
    }
}
