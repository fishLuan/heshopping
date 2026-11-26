package com.youkeda.hashopping.order.service;

import com.youkeda.hashopping.order.dao.OrderCloseDAO;
import com.youkeda.hashopping.order.model.OrderCloseRecord;
import com.youkeda.hashopping.product.service.StockService;
import com.youkeda.hashopping.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 用户取消与超时关闭共用事务入口，保证库存最多释放一次。 */
@Service
public class OrderCloseService {
    private final OrderCloseDAO orders;
    private final StockService stock;

    /** 注入订单数据库接口和库存服务。 */
    public OrderCloseService(OrderCloseDAO orders, StockService stock) {
        this.orders = orders;
        this.stock = stock;
    }

    /** 取消本人待付款订单；已关闭订单重复调用视为成功。 */
    @Transactional
    public void cancel(long userId, String orderNumber) {
        OrderCloseRecord order = orders.lock(orderNumber);
        if (order == null || !Long.toString(userId).equals(order.userId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "订单不存在");
        }
        if ("TRADE_CANCELLED".equals(order.status) || "TRADE_CLOSED".equals(order.status)) return;
        if (!"WAIT_BUYER_PAY".equals(order.status)) {
            throw new BusinessException(HttpStatus.CONFLICT, "ORDER_NOT_CANCELLABLE", "当前订单状态不能取消");
        }
        close(order, "TRADE_CANCELLED");
    }

    /** 定时任务逐笔调用，锁定后再次检查状态和数据库到期时间。 */
    @Transactional
    public void closeExpired(String orderNumber) {
        OrderCloseRecord order = orders.lock(orderNumber);
        if (order != null && order.expired && order.requestId != null
                && "WAIT_BUYER_PAY".equals(order.status)) {
            close(order, "TRADE_CLOSED");
        }
    }

    /** 更新状态并释放新流程预占库存，任一步失败则回滚整个事务。 */
    private void close(OrderCloseRecord order, String status) {
        if (orders.close(order.id, status) != 1) {
            throw new IllegalStateException("订单状态更新失败");
        }
        if (order.requestId != null) {
            stock.release(order.skuId, order.quantity);
        }
    }
}
