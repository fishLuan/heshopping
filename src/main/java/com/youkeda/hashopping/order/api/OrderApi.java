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
    private OrderService orderService;

    /**
     * 生成订单Api
     *
     * @return Result
     */
    @PostMapping(path = "/add")
    @ResponseBody
    public Result<Order> payOrder(@RequestBody Order order) {
        Result<Order> result = new Result();
        result.setSuccess(true);
        if (order == null) {
            result.setSuccess(false);
            result.setMessage("order is null");
            return result;
        }
        order.setUserId(String.valueOf(currentUser.requireUserId()));
        Order orderResult = orderService.add(order);
        result.setData(orderResult);
        return result;
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
