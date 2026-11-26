package com.youkeda.hashopping.cart.api;

import com.youkeda.hashopping.cart.service.CartService;
import com.youkeda.hashopping.cart.param.CartRequest;
import com.youkeda.hashopping.cart.model.CartItem;
import com.youkeda.hashopping.common.model.Result;
import com.youkeda.hashopping.security.context.CurrentUser;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** 本人购物车接口，所有操作从会话读取用户身份。 */
@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService carts;
    private final CurrentUser current;
    /** 注入购物车服务与身份入口。 */
    public CartController(CartService carts,CurrentUser current) { this.carts=carts; this.current=current; }
    /** 查询本人购物车。 */
    @GetMapping
    public Result<List<CartItem>> list() { return Result.success(carts.list(current.requireUserId())); }
    /** 加入规格，已存在时合并数量。 */
    @PostMapping
    public Result<Void> add(@RequestBody CartRequest request) {
        carts.add(current.requireUserId(),request.skuId,request.quantity);
        return Result.success();
    }
    /** 设置已有条目的数量。 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable long id,@RequestBody CartRequest request) {
        carts.update(current.requireUserId(),id,request.quantity);
        return Result.success();
    }
    /** 删除本人条目。 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable long id) {
        carts.delete(current.requireUserId(),id);
        return Result.success();
    }
}
