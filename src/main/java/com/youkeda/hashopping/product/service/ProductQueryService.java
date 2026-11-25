package com.youkeda.hashopping.product.service;

import com.youkeda.hashopping.common.exception.BusinessException;
import com.youkeda.hashopping.common.model.Paging;
import com.youkeda.hashopping.product.dao.ProductDAO;
import com.youkeda.hashopping.product.model.ProductCardResponse;
import com.youkeda.hashopping.product.param.ProductPageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;

/** 负责商城列表查询及参数规则，与商品维护服务分开。 */
@Service
public class ProductQueryService {
    private final ProductDAO products;

    /** 注入商品查询持久层。 */
    public ProductQueryService(ProductDAO products) {
        this.products = products;
    }

    /** 使用同一数据库快照查询总数和当前页，超出末页时返回空列表。 */
    @Transactional(readOnly = true)
    public Paging<ProductCardResponse> page(ProductPageRequest request) {
        if (request.getPageNum() < 1 || request.getPageSize() < 1 || request.getPageSize() > 100
                || !Arrays.asList("latest", "priceAsc", "priceDesc").contains(request.getSort())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT",
                    "页码至少为1，每页数量为1至100，排序支持latest、priceAsc、priceDesc");
        }
        if (request.getKeyword() != null) {
            request.setKeyword(request.getKeyword().trim());
            if (request.getKeyword().length() > 64) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", "搜索关键词不能超过64个字符");
            }
        }
        long total = products.countCards(request);
        int pages = (int) ((total + request.getPageSize() - 1) / request.getPageSize());
        return new Paging<>(request.getPageNum(), request.getPageSize(), pages, total,
                products.findCards(request));
    }
}
