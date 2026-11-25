package com.youkeda.hashopping.payment.api;

import com.youkeda.hashopping.payment.model.PaymentRecord;
import com.youkeda.hashopping.payment.model.PaymentStatus;
import com.youkeda.hashopping.common.model.Result;
import com.youkeda.hashopping.payment.param.PaymentParam;
import com.youkeda.hashopping.payment.service.PayService;
import com.youkeda.hashopping.payment.service.PaymentRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.youkeda.hashopping.order.model.Order;
import com.youkeda.hashopping.order.service.OrderService;
import org.springframework.http.HttpStatus;
import com.youkeda.hashopping.common.exception.BusinessException;
import com.youkeda.hashopping.security.context.CurrentUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(path = "/api/alipay")
/**
 * 支付处理与支付记录管理：PaymentApi。
 */
public class PaymentApi {
    @Autowired
    private CurrentUser currentUser;

    private static final Logger logger = LoggerFactory.getLogger(PaymentApi.class);

    @Autowired
    private PayService payService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentRecordService paymentRecordService;

    /** 校验当前用户拥有该订单，并使用订单中的金额发起付款。 */
    @PostMapping(path = "/pay")
    @ResponseBody
    public Result payOrder(@RequestBody PaymentParam paymentParam) {
        Order order = orderService.getByOrderNumber(paymentParam.getOrderNumber());
        if (order == null || !String.valueOf(currentUser.requireUserId()).equals(order.getUserId())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "订单不存在");
        }
        Double amount = order.getTotalPrice();
        if (amount == null || amount <= 0) {
            return Result.error("INVALID_ORDER_AMOUNT", "订单金额无效");
        }
        paymentParam.setUserId(String.valueOf(currentUser.requireUserId()));
        paymentParam.setPayAmount(amount);
        paymentParam.setTotalAmount(amount);
        PaymentRecord paymentRecord = initPaymentRecord(paymentParam);
        PaymentRecord saveResult = paymentRecordService.save(paymentRecord);
        if (saveResult == null) {
            logger.error("支付流水记录插入失败！");
        }
        return payService.payOrder(paymentParam);
    }

    /**
     * 组装支付记录
     *
     * @param paymentParam paymentParam
     * @return PaymentRecord
     */
    private PaymentRecord initPaymentRecord(PaymentParam paymentParam) {
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setPayType(paymentParam.getPayType());
        paymentRecord.setPayStatus(PaymentStatus.PENDING);
        paymentRecord.setUserId(paymentParam.getUserId());
        paymentRecord.setOrderNumber(paymentParam.getOrderNumber());
        paymentRecord.setAmount(paymentParam.getPayAmount());
        return paymentRecord;
    }
}
