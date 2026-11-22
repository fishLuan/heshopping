package com.youkeda.hashopping.product.dao;

import com.youkeda.hashopping.product.dataobject.ProductDetailDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
/**
 * 商品及商品详情管理：ProductDetailDAO。
 */
public interface ProductDetailDAO {
    List<ProductDetailDO> selectByIds(List<String> ids);
    int deleteByPrimaryKey(String id);

    int insert(ProductDetailDO record);

    ProductDetailDO selectByPrimaryKey(String id);

    List<ProductDetailDO> selectAll();

    List<ProductDetailDO> selectByProductId(String productId);

    int updateByPrimaryKey(ProductDetailDO record);
}
