package com.youkeda.hashopping.product.dao;

import com.youkeda.hashopping.product.dataobject.ProductDO;
import com.youkeda.hashopping.common.param.BasePageParam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
/**
 * 商品及商品详情管理：ProductDAO。
 */
public interface ProductDAO {
    int deleteByPrimaryKey(String id);

    int insert(ProductDO record);

    ProductDO selectByPrimaryKey(String id);

    List<ProductDO> selectAll();

    int updateByPrimaryKey(ProductDO record);

    int selectAllCounts();

    List<ProductDO> pageQuery(BasePageParam param);

}