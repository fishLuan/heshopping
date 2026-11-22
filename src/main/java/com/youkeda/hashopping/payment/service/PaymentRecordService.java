package com.youkeda.hashopping.payment.service;

import com.youkeda.hashopping.payment.model.PaymentRecord;
import com.youkeda.hashopping.payment.param.PaymentRecordQueryParam;

import java.util.List;

/**
 * @Author songchuanming
 * @DATE 2020/7/7.
 */
/**
 * 支付处理与支付记录管理：PaymentRecordService。
 */
public interface PaymentRecordService {
    /**
     * 添加或修改支付记录
     *
     * @return PaymentRecord
     */
    PaymentRecord save(PaymentRecord paymentRecord);

    /**
     * 查询支付记录
     *
     * @param queryParam 查询参数
     * @return List<PaymentRecord>
     */
    List<PaymentRecord> query(PaymentRecordQueryParam queryParam);

    /**
     * 更新状态
     *
     * @param paymentRecord 支付记录
     * @return PaymentRecord
     */
    PaymentRecord updateStatus(PaymentRecord paymentRecord);

}
