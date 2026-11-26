package com.youkeda.hashopping.order.dao;

import com.youkeda.hashopping.order.model.CreateOrderResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 新订单的幂等查询和快照写入。 */
@Mapper
public interface OrderCreationDAO {
    /** 按买家和请求标识锁定读取原订单，使用当前读避免并发快照遗漏。 */
    CreateOrderResponse find(@Param("userId") String userId, @Param("requestId") String requestId);

    /** 写入服务端生成的价格、数量和商品快照。 */
    int insert(CreateOrderResponse order);
}
