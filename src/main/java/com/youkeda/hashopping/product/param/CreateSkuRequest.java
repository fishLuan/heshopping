package com.youkeda.hashopping.product.param;

import java.math.BigDecimal;

/** 商家创建单个规格的输入，金额限制两位小数。 */
public class CreateSkuRequest {
    public String style;
    public BigDecimal size;
    public BigDecimal price;
    public Integer stock;
}
