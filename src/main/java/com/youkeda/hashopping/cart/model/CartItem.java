package com.youkeda.hashopping.cart.model;

import com.youkeda.hashopping.product.model.CartProduct;

/** 本人购物车条目，实时附带商品状态与不可购买原因。 */
public class CartItem {
    public Long id;
    public String skuId;
    public Integer quantity;
    public CartProduct product;
    public boolean available;
    public String unavailableReason;
}
