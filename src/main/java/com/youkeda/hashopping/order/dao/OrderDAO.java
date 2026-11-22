package com.youkeda.hashopping.order.dao;

import com.youkeda.hashopping.order.dataobject.OrderDO;
import com.youkeda.hashopping.order.param.QueryOrderParam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
/**
 * 订单管理：OrderDAO。
 */
public interface OrderDAO {
    int insert(OrderDO order);

    int selectCounts(QueryOrderParam param);

    List<OrderDO> pageQuery(QueryOrderParam param);

    OrderDO selectByOrderNumber(String orderNumber);

    int update(OrderDO orderDO);
}
