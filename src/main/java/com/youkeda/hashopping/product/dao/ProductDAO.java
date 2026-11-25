package com.youkeda.hashopping.product.dao;

import com.youkeda.hashopping.product.dataobject.ProductDO;
import com.youkeda.hashopping.common.param.BasePageParam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import com.youkeda.hashopping.product.param.ProductPageRequest;
import com.youkeda.hashopping.product.model.ProductCardResponse;

@Mapper
/**
 * 商品及商品详情管理：ProductDAO。
 */
public interface ProductDAO {
    /** 统计营业店铺中有库存SKU的匹配商品数。 */
    long countCards(ProductPageRequest request);

    /** 查询卡片，并按可售SKU最低价排序。 */
    List<ProductCardResponse> findCards(ProductPageRequest request);

    int deleteByPrimaryKey(String id);

    int insert(ProductDO record);

    ProductDO selectByPrimaryKey(String id);

    List<ProductDO> selectAll();

    int updateByPrimaryKey(ProductDO record);

    int selectAllCounts();

    List<ProductDO> pageQuery(BasePageParam param);

}