package com.youkeda.hashopping.payment.service;

import com.youkeda.hashopping.common.model.Result;
import com.youkeda.hashopping.payment.param.PaymentParam;

import java.util.Map;

/**
 * 支付处理与支付记录管理：PayService。
 */
public interface PayService {
    /**
     * 支付订单
     *
     * @param paymentParam 支付所需参数
     * @return Result
     */
    Result payOrder(PaymentParam paymentParam);

    /**
     * 支付宝回调接口
     *
     * @param mapParam 支付宝参数
     * @return Result
     */
    Result alipayCallBack(Map<String, String> mapParam);
}
