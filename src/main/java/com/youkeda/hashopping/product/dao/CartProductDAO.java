package com.youkeda.hashopping.product.dao;

import com.youkeda.hashopping.product.model.CartProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/** 为购物车提供批量商品信息查询。 */
@Mapper
public interface CartProductDAO {
    /** 批量读取规格及当前商品、店铺状态。 */
    java.util.List<CartProduct> findByIds(@Param("ids") List<String> ids);
}
