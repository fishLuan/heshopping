package com.youkeda.hashopping.product.api;

import com.youkeda.hashopping.common.model.Paging;
import com.youkeda.hashopping.product.model.Product;
import com.youkeda.hashopping.common.model.Result;
import com.youkeda.hashopping.common.param.BasePageParam;
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

    @Autowired
    private ProductService productService;

    @ResponseBody
    @GetMapping("/page")
    public Result<Paging<Product>> pageQuery(BasePageParam param) {

        Result<Paging<Product>> result = new Result<>();

        result.setSuccess(true);
        result.setData(productService.pageQueryProduct(param));
        return result;
    }

}
