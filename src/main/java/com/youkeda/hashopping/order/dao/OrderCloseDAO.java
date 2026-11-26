package com.youkeda.hashopping.order.dao;

import com.youkeda.hashopping.order.model.OrderCloseRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/** 订单关闭和到期扫描的数据库接口，SQL统一放在XML。 */
@Mapper
public interface OrderCloseDAO {
    /** 按订单号锁定订单，数据库时间判断是否到期。 */
    OrderCloseRecord lock(@Param("orderNumber") String orderNumber);
    /** 仅允许待付款订单转换到关闭状态。 */
    int close(@Param("id") String id, @Param("status") String status);
    /** 分批读取到期且确实预占了库存的订单。 */
    List<String> findExpired();
}
