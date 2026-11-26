package com.youkeda.hashopping.order.api;

import com.youkeda.hashopping.order.service.OrderQueryService;
import com.youkeda.hashopping.order.model.CreateOrderResponse;
import com.youkeda.hashopping.common.model.Result;
import com.youkeda.hashopping.common.model.Paging;
import com.youkeda.hashopping.security.context.CurrentUser;
import org.springframework.web.bind.annotation.*;

/** 本人购买订单查询入口。 */
@RestController
@RequestMapping("/api/order")
public class OrderQueryController {
    private final CurrentUser current;
    private final OrderQueryService queries;
    /** 注入身份和订单查询服务。 */
    public OrderQueryController(CurrentUser current, OrderQueryService queries) {
        this.current = current;
        this.queries = queries;
    }
    /** 分页查询本人订单，页码从1开始。 */
    @GetMapping("/mine")
    public Result<Paging<CreateOrderResponse>> mine(@RequestParam(defaultValue="1") int pageNum,
                                                   @RequestParam(required=false) String status) {
        return Result.success(queries.page(current.requireUserId(), pageNum, status));
    }
    /** 查询本人订单详情，不能读取其他买家的订单。 */
    @GetMapping("/{number}")
    public Result<CreateOrderResponse> detail(@PathVariable String number) {
        return Result.success(queries.detail(current.requireUserId(), number));
    }
}
