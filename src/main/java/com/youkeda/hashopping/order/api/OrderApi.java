package com.youkeda.hashopping.order.api;

import com.youkeda.hashopping.order.model.Order;
import com.youkeda.hashopping.security.context.CurrentUser;
import com.youkeda.hashopping.common.model.Paging;
import com.youkeda.hashopping.common.model.Result;
import com.youkeda.hashopping.order.param.QueryOrderParam;
import com.youkeda.hashopping.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(path = "/api/order")
/**
 * 订单管理：OrderApi。
 */
public class OrderApi {

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private com.youkeda.hashopping.order.service.OrderCloseService closer;

    /** 取消当前登录用户的待付款订单。 */
    @PostMapping("/{orderNumber}/cancel")
    @ResponseBody
    public Result<Void> cancel(@PathVariable("orderNumber") String orderNumber) {
        closer.cancel(currentUser.requireUserId(), orderNumber);
        return Result.success("OK", "订单已取消", null);
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private com.youkeda.hashopping.order.service.OrderCreationService creation;

    /** 使用当前会话的买家身份创建单SKU订单。 */
    @PostMapping("/add")
    @ResponseBody
    public Result<com.youkeda.hashopping.order.model.CreateOrderResponse> create(
            @RequestBody com.youkeda.hashopping.order.param.CreateOrderRequest request) {
        return Result.success(creation.create(currentUser.requireUserId(), request));
    }
    /**
     * 查询支付成功订单APi
     *
     * @return Result
     */
    @GetMapping(path = "/queryrecentpaysuccess")
    @ResponseBody
    public Result<Paging<Order>> queryRecentPaySuccess(@RequestBody QueryOrderParam queryOrderParam) {
        Result<Paging<Order>> result = new Result();
        result.setSuccess(true);
        if (queryOrderParam == null) {
            result.setSuccess(false);
            result.setMessage("queryOrderParam is null");
            return result;
        }

        result.setData(orderService.queryRecentPaySuccess(queryOrderParam));
        return result;
    }
}
