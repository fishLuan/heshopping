package com.youkeda.hashopping.order.model;

/** 关闭订单时锁定读取的数据，历史订单可无预占标记。 */
public class OrderCloseRecord {
    public String id;
    public String userId;
    public String skuId;
    public Integer quantity;
    public String requestId;
    public String status;
    public boolean expired;
}
