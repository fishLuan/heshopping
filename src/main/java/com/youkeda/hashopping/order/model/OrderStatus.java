package com.youkeda.hashopping.order.model;


/**
 * 订单管理：OrderStatus。
 */
public enum  OrderStatus {
    /**
     * 待付款
     */
    WAIT_BUYER_PAY,

    /**
     * 订单关闭
     */
    TRADE_CLOSED,

    /** 用户主动取消。 */
    TRADE_CANCELLED,

    /**
     * 订单已支付成功
     */
    TRADE_PAID_SUCCESS,

    /**
     * 订单支付失败
     */
    TRADE_PAID_FAILED,
}
