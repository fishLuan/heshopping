package com.youkeda.hashopping.order.param;

/** 下单输入，买家身份和价格由服务端确定。 */
public class CreateOrderRequest {
    /** 当前买家选择的收货地址。 */
    public Long addressId;
    public String skuId;
    public Integer quantity;
    public String requestId;
}
