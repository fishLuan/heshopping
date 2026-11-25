package com.youkeda.hashopping.product.model;

import java.math.BigDecimal;

/** 商城商品卡片，仅包含列表展示字段。 */
public class ProductCardResponse {
    public String id;
    public String name;
    public String mainImage;
    public BigDecimal price;
    public int purchaseNum;
    public Long shopId;
    public String shopName;
}
