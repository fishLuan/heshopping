package com.youkeda.hashopping.product.service;

import com.youkeda.hashopping.product.dao.StockDAO;
import com.youkeda.hashopping.product.model.PurchasableSku;
import com.youkeda.hashopping.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 提供订单模块使用的库存预占能力。 */
@Service
public class StockService {
    private final StockDAO stock;

    /** 注入库存持久层。 */
    public StockService(StockDAO stock) { this.stock = stock; }

    /** 与关单同一事务归还库存，库存记录缺失时整体回滚。 */
    @Transactional(propagation = Propagation.MANDATORY)
    public void release(String skuId, int quantity) {
        if (quantity < 1 || stock.release(skuId, quantity) != 1) {
            throw new IllegalStateException("无法归还订单库存");
        }
    }

    /** 必须在已有事务中预占库存，创建订单失败时会一同回滚。 */
    @Transactional(propagation = Propagation.MANDATORY)
    public PurchasableSku reserve(String skuId, int quantity) {
        PurchasableSku sku = stock.lockSku(skuId);
        if (sku == null || sku.price == null || sku.price.signum() <= 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "SKU_UNAVAILABLE", "商品规格不可购买");
        }
        if (stock.reserve(skuId, quantity) != 1) {
            throw new BusinessException(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", "库存不足");
        }
        return sku;
    }
}
