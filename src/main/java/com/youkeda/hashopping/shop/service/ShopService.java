package com.youkeda.hashopping.shop.service;

import com.youkeda.hashopping.shop.dao.ShopDAO;
import com.youkeda.hashopping.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

/** 提供店铺归属校验，商品模块不直接访问店铺DAO。 */
@Service
public class ShopService {
    private final ShopDAO shops;
    /** 注入店铺查询。 */
    public ShopService(ShopDAO shops) { this.shops=shops; }
    /** 锁定本人营业店铺，不泄露其他经营者的店铺信息。 */
    @Transactional(propagation=Propagation.MANDATORY)
    public void requireOwnedActive(long userId,long shopId) {
        if(shops.lockOwnedActive(userId,shopId)==null)
            throw new BusinessException(HttpStatus.NOT_FOUND,"SHOP_UNAVAILABLE","店铺不存在、非本人经营或已停用");
    }
}
