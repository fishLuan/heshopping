package com.youkeda.hashopping.order.dao;

import com.youkeda.hashopping.order.model.CreateOrderResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 支付确认与关闭订单竞争同一行锁。 */
@Mapper
public interface OrderPaymentDAO {
    /** 当前读锁定订单并返回成交金额。 */
    CreateOrderResponse lock(@Param("number") String number);
    /** 数据库时间检查是否已到付款截止时间。 */
    boolean expired(@Param("id") String id);
    /** 仅将未到期的待付款订单改为已付款。 */
    int markPaid(@Param("id") String id);
}
