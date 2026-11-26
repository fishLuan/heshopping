package com.youkeda.hashopping.order.dao;

import com.youkeda.hashopping.order.model.CreateOrderResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/** 本人订单查询，买家条件在每条SQL中强制应用。 */
@Mapper
public interface OrderQueryDAO {
    /** 查询本人订单数量。 */
    long count(@Param("userId") String userId, @Param("status") String status);
    /** 按创建时间倒序查询本人订单。 */
    List<CreateOrderResponse> page(@Param("userId") String userId, @Param("status") String status,
                                  @Param("offset") long offset);
    /** 查询本人的指定订单。 */
    CreateOrderResponse detail(@Param("userId") String userId, @Param("number") String number);
}
