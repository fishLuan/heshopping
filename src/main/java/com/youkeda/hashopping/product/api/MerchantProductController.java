package com.youkeda.hashopping.product.api;

import com.youkeda.hashopping.product.param.CreateProductRequest;
import com.youkeda.hashopping.product.model.CreateProductResponse;
import com.youkeda.hashopping.product.service.ProductCreationService;
import com.youkeda.hashopping.security.context.CurrentUser;
import com.youkeda.hashopping.common.model.Result;
import org.springframework.web.bind.annotation.*;

/** 商家商品维护入口，权限由Spring Security校验。 */
@RestController
@RequestMapping("/api/merchant/products")
public class MerchantProductController {
    private final ProductCreationService creation;
    private final CurrentUser current;
    /** 注入商品创建服务和当前身份组件。 */
    public MerchantProductController(ProductCreationService creation,CurrentUser current) {
        this.creation=creation; this.current=current;
    }
    /** 为本人店铺创建商品及规格，身份不接受客户端指定。 */
    @PostMapping
    public Result<CreateProductResponse> create(@RequestBody CreateProductRequest request) {
        return Result.success(creation.create(current.requireUserId(),request));
    }
}
