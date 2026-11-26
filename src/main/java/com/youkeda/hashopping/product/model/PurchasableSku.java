package com.youkeda.hashopping.product.model;

import java.math.BigDecimal;

/** 下单时锁定读取的SKU及商品、店铺信息。 */
public class PurchasableSku {
    public String id;
    public BigDecimal price;
    public Integer stock;
    public Long shopId;
    public String productName;
    public String skuSnapshot;
}
