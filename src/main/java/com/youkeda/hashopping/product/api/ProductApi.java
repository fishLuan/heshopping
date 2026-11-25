package com.youkeda.hashopping.product.api;

import com.youkeda.hashopping.common.model.Paging;
import com.youkeda.hashopping.product.model.Product;
import com.youkeda.hashopping.common.model.Result;
import com.youkeda.hashopping.product.param.ProductPageRequest;
import com.youkeda.hashopping.product.model.ProductCardResponse;
import com.youkeda.hashopping.product.service.ProductQueryService;
import com.youkeda.hashopping.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/product")
@Controller
/**
 * 商品及商品详情管理：ProductApi。
 */
public class ProductApi {

    private final ProductQueryService queries;

    @Autowired
    private ProductService products;

    /** 按主键读取公开商品详情，商品不存在时返回统一的404。 */
    @GetMapping("/{id}")
    @ResponseBody
    public Result<Product> detail(@org.springframework.web.bind.annotation.PathVariable("id") String id) {
        Product product = products.get(id);
        if (product == null) {
            throw new com.youkeda.hashopping.common.exception.BusinessException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "商品不存在");
        }
        return Result.success(product);
    }

    /** 注入商城列表查询服务。 */
    public ProductApi(ProductQueryService queries) {
        this.queries = queries;
    }

    /** 游客可访问的商品分页接口，默认每页12条。 */
    @ResponseBody
    @GetMapping("/page")
    public Result<Paging<ProductCardResponse>> pageQuery(ProductPageRequest request) {
        return Result.success(queries.page(request));
    }
}