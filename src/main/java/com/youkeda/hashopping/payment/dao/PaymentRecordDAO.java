package com.youkeda.hashopping.payment.dao;

import com.youkeda.hashopping.payment.dataobject.PaymentRecordDO;
import com.youkeda.hashopping.payment.param.PaymentRecordQueryParam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
/**
 * 支付处理与支付记录管理：PaymentRecordDAO。
 */
public interface PaymentRecordDAO {

    int insert(PaymentRecordDO record);

    List<PaymentRecordDO> select(PaymentRecordQueryParam paymentRecordQueryParam);

    int update(PaymentRecordDO record);
}