package com.youkeda.hashopping.product.param;

import java.util.List;

/** 商家创建商品的完整请求，不接收商品ID和销量。 */
public class CreateProductRequest {
    public Long shopId;
    public String name;
    public String productIntro;
    public List<String> productImgs;
    public List<CreateSkuRequest> skus;
}
