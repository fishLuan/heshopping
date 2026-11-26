package com.youkeda.hashopping.product.service;

import com.youkeda.hashopping.product.dao.ProductSalesDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

/** 支付确认时增加销量，参与订单事务。 */
@Service
public class ProductSalesService {
    private final ProductSalesDAO sales;
    /** 注入商品销量持久层。 */
    public ProductSalesService(ProductSalesDAO sales) { this.sales = sales; }
    /** 原子增加实际成交件数，失败时回滚付款事务。 */
    @Transactional(propagation=Propagation.MANDATORY)
    public void increase(String skuId, int quantity) {
        if (quantity < 1 || sales.increase(skuId,quantity) != 1)
            throw new IllegalStateException("商品销量更新失败");
    }
}
