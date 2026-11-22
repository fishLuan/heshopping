package com.youkeda.hashopping.product.service;

import com.youkeda.hashopping.common.model.Paging;
import com.youkeda.hashopping.product.model.Product;
import com.youkeda.hashopping.common.param.BasePageParam;

/**
 * 商品及商品详情管理：ProductService。
 */
public interface ProductService {

    /**
     * 增加或修改商品
     *
     * @param product 商品
     * @return Product
     */
    int save(Product product);

    /**
     * 分页查询商品
     *
     * @param param 商品分页参数
     * @return PagingData<Product>
     */
    Paging<Product> pageQueryProduct(BasePageParam param);

    /**
     * 根据主键id获取商品信息
     *
     * @param id 主键id
     * @return Product
     */
    Product get(String id);
}
