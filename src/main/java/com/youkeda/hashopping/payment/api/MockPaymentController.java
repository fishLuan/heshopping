package com.youkeda.hashopping.payment.api;

import com.youkeda.hashopping.payment.service.MockPaymentService;
import com.youkeda.hashopping.payment.param.MockPaymentRequest;
import com.youkeda.hashopping.order.model.CreateOrderResponse;
import com.youkeda.hashopping.common.model.Result;
import com.youkeda.hashopping.security.context.CurrentUser;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

/** 仅开发和测试环境提供的模拟支付入口。 */
@RestController
@Profile({"dev","test"})
@RequestMapping("/api/payment/mock")
public class MockPaymentController {
    private final MockPaymentService payments;
    private final CurrentUser current;
    /** 注入模拟渠道与当前会话身份。 */
    public MockPaymentController(MockPaymentService payments, CurrentUser current) {
        this.payments = payments;
        this.current = current;
    }
    /** 付款失败保留待付款状态，成功时返回已付款订单。 */
    @PostMapping("/pay")
    public Result<CreateOrderResponse> pay(@RequestBody MockPaymentRequest request) {
        CreateOrderResponse order = payments.pay(current.requireUserId(),request);
        if ("TRADE_PAID_SUCCESS".equals(order.status))
            return Result.success("OK","模拟支付成功",order);
        return Result.<CreateOrderResponse>warning("MOCK_PAYMENT_FAILED","模拟支付失败，可重试或取消订单").setData(order);
    }
}
