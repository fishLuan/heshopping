package com.youkeda.hashopping.order.service;

import com.youkeda.hashopping.order.dao.OrderPaymentDAO;
import com.youkeda.hashopping.order.model.CreateOrderResponse;
import com.youkeda.hashopping.product.service.ProductSalesService;
import com.youkeda.hashopping.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

/** 支付业务规则与订单状态转换，不依赖模拟渠道实现。 */
@Service
public class OrderPaymentService {
    private final OrderPaymentDAO orders;
    private final ProductSalesService sales;
    /** 注入订单锁定查询和销量服务。 */
    public OrderPaymentService(OrderPaymentDAO orders, ProductSalesService sales) {
        this.orders = orders;
        this.sales = sales;
    }
    /** 验证本人订单，锁定持续到外层支付事务结束。 */
    @Transactional(propagation=Propagation.MANDATORY)
    public CreateOrderResponse prepare(long userId, String number) {
        CreateOrderResponse order = orders.lock(number);
        if (order == null || !Long.toString(userId).equals(order.userId))
            throw new BusinessException(HttpStatus.NOT_FOUND,"ORDER_NOT_FOUND","订单不存在");
        if ("TRADE_PAID_SUCCESS".equals(order.status)) return order;
        if (!"WAIT_BUYER_PAY".equals(order.status))
            throw new BusinessException(HttpStatus.CONFLICT,"ORDER_NOT_PAYABLE","当前订单不能支付");
        if (order.requestId == null || orders.expired(order.id))
            throw new BusinessException(HttpStatus.CONFLICT,"ORDER_EXPIRED","订单已到期或不支持当前付款流程");
        if (order.totalPrice == null || order.totalPrice.signum() <= 0)
            throw new BusinessException(HttpStatus.CONFLICT,"INVALID_ORDER_AMOUNT","订单金额无效");
        return order;
    }
    /** 状态和销量同事务提交，重复付款不会再次增加销量。 */
    @Transactional(propagation=Propagation.MANDATORY)
    public void markPaid(CreateOrderResponse order) {
        if ("TRADE_PAID_SUCCESS".equals(order.status)) return;
        if (orders.markPaid(order.id) != 1)
            throw new BusinessException(HttpStatus.CONFLICT,"ORDER_EXPIRED","订单已超过付款时间");
        sales.increase(order.skuId,order.quantity);
        order.status = "TRADE_PAID_SUCCESS";
    }
}
