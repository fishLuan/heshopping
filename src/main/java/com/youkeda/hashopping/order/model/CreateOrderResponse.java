package com.youkeda.hashopping.order.model;

import java.math.BigDecimal;

/** 下单结果，同时用于幂等重试时返回原订单。 */
public class CreateOrderResponse {
    public java.time.LocalDateTime expiresAt;
    public java.time.LocalDateTime createdAt;
    @com.fasterxml.jackson.annotation.JsonIgnore
    public long timeoutMinutes;
    /** 下单地址及收货信息快照，历史订单允许为空。 */
    public Long addressId;
    public String recipientName;
    public String recipientPhone;
    public String shippingAddress;
    public String id;
    public String orderNumber;
    public String userId;
    public String skuId;
    public Integer quantity;
    public BigDecimal unitPrice;
    public BigDecimal totalPrice;
    public Long shopId;
    public String productName;
    public String skuSnapshot;
    public String requestId;
    public String status;
}
