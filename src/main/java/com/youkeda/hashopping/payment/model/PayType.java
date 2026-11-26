package com.youkeda.hashopping.payment.model;

/**
 * @Author songchuanming
 * @DATE 2020/7/6.
 */
/**
 * 支付处理与支付记录管理：PayType。
 */
public enum PayType {
    /** 开发测试模拟渠道。 */
    MOCK,
    /**
     * 微信
     */
    WEIXIN,
    /**
     * 支付宝
     */
    ALIPAY
}
