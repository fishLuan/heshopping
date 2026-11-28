package com.youkeda.hashopping.shop.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 店铺归属数据库接口。 */
@Mapper
public interface ShopDAO {
    /** 锁定本人正常营业的店铺，直到创建商品事务结束。 */
    Long lockOwnedActive(@Param("userId") long userId,@Param("shopId") long shopId);
}
