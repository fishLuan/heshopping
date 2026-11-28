package com.youkeda.hashopping.product.model;

import java.util.ArrayList;
import java.util.List;

/** 创建成功后返回商品主键和SKU主键。 */
public class CreateProductResponse {
    public String productId;
    public List<String> skuIds=new ArrayList<>();
}
