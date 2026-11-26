package com.youkeda.hashopping.product.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 商品销量原子增量更新。 */
@Mapper
public interface ProductSalesDAO {
    /** 由SKU定位商品并增加购买数量。 */
    int increase(@Param("skuId") String skuId, @Param("quantity") int quantity);
}
