package com.youkeda.hashopping.product.service;

import com.youkeda.hashopping.product.dao.CartProductDAO;
import com.youkeda.hashopping.product.model.CartProduct;
import org.springframework.stereotype.Service;
import java.util.*;

/** 商品模块向购物车提供只读展示信息，避免跨模块访问DAO。 */
@Service
public class CartProductService {
    private final CartProductDAO products;
    /** 注入商品批量查询。 */
    public CartProductService(CartProductDAO products) { this.products = products; }
    /** 按SKU编号返回当前数据，失效SKU缺席。 */
    public Map<String,CartProduct> find(List<String> ids) {
        Map<String,CartProduct> result = new HashMap<>();
        if (!ids.isEmpty()) for (CartProduct item : products.findByIds(ids)) result.put(item.skuId,item);
        return result;
    }
}
