package com.youkeda.hashopping.cart.service;

import com.youkeda.hashopping.cart.dao.CartDAO;
import com.youkeda.hashopping.cart.model.CartItem;
import com.youkeda.hashopping.product.model.CartProduct;
import com.youkeda.hashopping.product.service.CartProductService;
import com.youkeda.hashopping.user.service.BuyerService;
import com.youkeda.hashopping.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/** 购物车合并和归属规则，写操作按用户行锁串行化，不修改库存。 */
@Service
public class CartService {
    private final CartDAO carts;
    private final CartProductService products;
    private final BuyerService buyers;
    /** 注入各模块的公开能力。 */
    public CartService(CartDAO carts,CartProductService products,BuyerService buyers) {
        this.carts=carts; this.products=products; this.buyers=buyers;
    }
    /** 批量附上实时价格和不可售原因，失效条目仍然可见。 */
    public List<CartItem> list(long userId) {
        List<CartItem> items=carts.list(userId);
        Map<String,CartProduct> values=products.find(items.stream().map(item->item.skuId).collect(Collectors.toList()));
        for(CartItem item:items) {
            item.product=values.get(item.skuId);
            item.unavailableReason=reason(item.product,item.quantity);
            item.available=item.unavailableReason==null;
        }
        return items;
    }
    /** 同一用户同一SKU累加数量，在用户事务锁内防止并发丢失更新。 */
    @Transactional
    public void add(long userId,String skuId,Integer quantity) {
        validate(quantity);
        if(skuId==null || !skuId.matches("[A-Za-z0-9_-]{1,32}")) fail("请选择有效规格");
        buyers.lockActive(userId);
        CartItem old=carts.findSku(userId,skuId);
        long total=(long)quantity+(old==null?0:old.quantity);
        if(total>9999) fail("单个规格最多加购9999件");
        check(skuId,(int)total);
        if(old==null) carts.insert(userId,skuId,(int)total);
        else carts.update(userId,old.id,(int)total);
    }
    /** 修改数量时重新验证当前库存，订单创建时仍需再次验证。 */
    @Transactional
    public void update(long userId,long id,Integer quantity) {
        validate(quantity);
        buyers.lockActive(userId);
        CartItem item=get(userId,id);
        check(item.skuId,quantity);
        carts.update(userId,id,quantity);
    }
    /** 删除本人条目，失效商品同样允许清理。 */
    @Transactional
    public void delete(long userId,long id) {
        buyers.lockActive(userId);
        get(userId,id);
        carts.delete(userId,id);
    }
    /** 校验条目归属，不泄露其他用户数据。 */
    private CartItem get(long userId,long id) {
        CartItem item=carts.find(userId,id);
        if(item==null) throw new BusinessException(HttpStatus.NOT_FOUND,"CART_ITEM_NOT_FOUND","购物车条目不存在");
        return item;
    }
    /** 取得当前商品状态并拒绝不可售规格。 */
    private void check(String skuId,int quantity) {
        String message=reason(products.find(Collections.singletonList(skuId)).get(skuId),quantity);
        if(message!=null) throw new BusinessException(HttpStatus.CONFLICT,"SKU_UNAVAILABLE",message);
    }
    /** 描述失效或库存不足原因。 */
    private String reason(CartProduct product,int quantity) {
        if(product==null) return "商品规格已失效";
        if(!"ACTIVE".equals(product.shopStatus)) return "店铺已停止营业";
        if(product.price==null || product.price.signum()<=0) return "商品暂不可售";
        if(product.stock==null || product.stock<quantity) return "库存不足，请调整数量";
        return null;
    }
    /** 校验购买数量范围，防止累加溢出。 */
    private void validate(Integer quantity) {
        if(quantity==null || quantity<1 || quantity>9999) fail("数量需为1至9999的整数");
    }
    /** 返回统一参数异常。 */
    private void fail(String message) {
        throw new BusinessException(HttpStatus.BAD_REQUEST,"INVALID_ARGUMENT",message);
    }
}
