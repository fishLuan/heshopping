package com.youkeda.hashopping.cart.dao;

import com.youkeda.hashopping.cart.model.CartItem;
import org.apache.ibatis.annotations.*;
import java.util.List;

/** 只访问当前用户购物车条目，SQL保存在XML。 */
@Mapper
public interface CartDAO {
    /** 列出本人条目。 */
    List<CartItem> list(@Param("userId") long userId);
    /** 读取本人指定条目。 */
    CartItem find(@Param("userId") long userId,@Param("id") long id);
    /** 按本人和规格查找合并目标。 */
    CartItem findSku(@Param("userId") long userId,@Param("skuId") String skuId);
    /** 新增加购条目。 */
    int insert(@Param("userId") long userId,@Param("skuId") String skuId,@Param("quantity") int quantity);
    /** 设置本人条目的新数量。 */
    int update(@Param("userId") long userId,@Param("id") long id,@Param("quantity") int quantity);
    /** 删除本人条目。 */
    int delete(@Param("userId") long userId,@Param("id") long id);
}
