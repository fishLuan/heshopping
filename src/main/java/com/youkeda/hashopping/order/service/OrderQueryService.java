package com.youkeda.hashopping.order.service;

import com.youkeda.hashopping.order.dao.OrderQueryDAO;
import com.youkeda.hashopping.order.model.CreateOrderResponse;
import com.youkeda.hashopping.order.model.OrderStatus;
import com.youkeda.hashopping.common.model.Paging;
import com.youkeda.hashopping.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 验证筛选参数并读取本人订单。 */
@Service
public class OrderQueryService {
    private final OrderQueryDAO orders;
    /** 注入订单查询接口。 */
    public OrderQueryService(OrderQueryDAO orders) { this.orders = orders; }

    /** 在一致快照中查询分页，固定每页12笔。 */
    @Transactional(readOnly=true)
    public Paging<CreateOrderResponse> page(long userId, int page, String status) {
        if (page < 1) throw new BusinessException(HttpStatus.BAD_REQUEST,"INVALID_ARGUMENT","页码至少为1");
        if (status != null && !status.isEmpty()) {
            try { OrderStatus.valueOf(status); }
            catch (IllegalArgumentException ex) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,"INVALID_ARGUMENT","无效订单状态");
            }
        } else status = null;
        long total = orders.count(Long.toString(userId),status);
        return new Paging<>(page,12,(int)((total+11)/12),total,
                orders.page(Long.toString(userId),status,((long)page-1)*12));
    }

    /** 校验归属，不向请求方泄露他人订单是否存在。 */
    public CreateOrderResponse detail(long userId, String number) {
        CreateOrderResponse order = orders.detail(Long.toString(userId),number);
        if (order == null) throw new BusinessException(HttpStatus.NOT_FOUND,"ORDER_NOT_FOUND","订单不存在");
        return order;
    }
}
