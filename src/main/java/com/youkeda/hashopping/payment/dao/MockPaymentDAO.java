package com.youkeda.hashopping.payment.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;

/** 保存开发模拟支付尝试，与订单状态使用同一事务。 */
@Mapper
public interface MockPaymentDAO {
    /** 写入一笔成功或失败的模拟支付记录。 */
    int insert(@Param("id") String id, @Param("userId") String userId,
               @Param("number") String number, @Param("amount") BigDecimal amount,
               @Param("status") String status);
}
