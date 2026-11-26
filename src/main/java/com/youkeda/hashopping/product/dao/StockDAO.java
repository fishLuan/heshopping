package com.youkeda.hashopping.product.dao;

import com.youkeda.hashopping.product.model.PurchasableSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 库存预占持久层，参与调用方的数据库事务。 */
@Mapper
public interface StockDAO {
    /** 原子归还订单预占的库存，SKU不存在时返回零。 */
    int release(@Param("id") String id, @Param("quantity") int quantity);
    /** 锁定SKU并检查经营中的店铺，串行化同规格下单。 */
    PurchasableSku lockSku(@Param("id") String id);

    /** 库存充足时原子扣减，失败返回零。 */
    int reserve(@Param("id") String id, @Param("quantity") int quantity);
}
