package com.youkeda.hashopping.product.api;

import com.youkeda.hashopping.product.model.ProductDetail;
import com.youkeda.hashopping.common.model.Result;
import com.youkeda.hashopping.product.service.ProductDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * TODO
 *
 * @author zr
 * @date 2020/7/14, 周二
 */
@Controller
@RequestMapping("/api/productdetail")
/**
 * 商品及商品详情管理：ProductDetailApi。
 */
public class ProductDetailApi {

    @Autowired
    private ProductDetailService productDetailService;

    @GetMapping("/productId")
    @ResponseBody
    public Result<List<ProductDetail>> getProductDetails(@RequestParam("productId") String productId) {

        Result<List<ProductDetail>> result = new Result<>();

        List<ProductDetail> productDetails = productDetailService.getByProductId(productId);

        result.setData(productDetails);
        result.setSuccess(true);
        return result;

    }
}
