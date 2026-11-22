package com.youkeda.hashopping.payment.model;

/**
 * @Author jiaoheng
 * @DATE 2020-01-06
 */
/**
 * 支付处理与支付记录管理：PaymentStatus。
 */
public enum PaymentStatus {
    /**
     * 支付中
     */
    PENDING,
    /**
     * 支付成功
     */
    SUCCESS,
    /**
     * 支付失败
     */
    FAILURE,
    /**
     * 退款成功
     */
    REFUND_SUCCESS,
    /**
     * 退款失败
     */
    REFUND_FAILED
}
