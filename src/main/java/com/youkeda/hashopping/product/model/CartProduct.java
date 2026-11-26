package com.youkeda.hashopping.product.model;

import java.math.BigDecimal;

/** 购物车所需的当前商品展示数据，不表示锁价或库存预占。 */
public class CartProduct {
    public String skuId;
    public String productId;
    public String name;
    public String image;
    public String size;
    public BigDecimal price;
    public Integer stock;
    public String shopName;
    public String shopStatus;
}
