package com.youkeda.hashopping.product.service.impl;

import com.youkeda.hashopping.product.dao.ProductDetailDAO;
import com.youkeda.hashopping.product.dataobject.ProductDetailDO;
import com.youkeda.hashopping.product.model.ProductDetail;
import com.youkeda.hashopping.product.service.ProductDetailService;
import com.youkeda.hashopping.common.util.UUIDUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
/**
 * 商品及商品详情管理：ProductDetailServiceImpl。
 */
public class ProductDetailServiceImpl implements ProductDetailService {
    @Autowired
    private ProductDetailDAO productDetailDAO;

    @Override
    public List<ProductDetail> queryProductDetail(List<String> productDetailIds) {
        if (CollectionUtils.isEmpty(productDetailIds)) {
            return null;
        }
        List<ProductDetailDO> productDetailDOS = productDetailDAO.selectByIds(productDetailIds);
        List<ProductDetail> productDetails = new ArrayList<>();
        if (CollectionUtils.isEmpty(productDetailDOS)) {
            return productDetails;
        }
        for (ProductDetailDO productDetailDO : productDetailDOS) {
            productDetails.add(productDetailDO.convertToModel());
        }
        return productDetails;
    }

    /** 保存规格并校验商家款式名称，新建时缺省为标准款。 */
    @Override
    public ProductDetail save(ProductDetail productDetail) {
        if (productDetail.getStyle() != null) {
            String style = productDetail.getStyle().trim();
            if (style.isEmpty() || style.length() > 64) {
                throw new com.youkeda.hashopping.common.exception.BusinessException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,"INVALID_ARGUMENT","款式需为1至64个字符");
            }
            productDetail.setStyle(style);
        }

        if (StringUtils.isBlank(productDetail.getId())) {
            if (productDetail.getStyle() == null) productDetail.setStyle("标准款");
            productDetail.setId(UUIDUtils.getUUID());

            productDetailDAO.insert(new ProductDetailDO(productDetail));
            return productDetail;
        }

        productDetailDAO.updateByPrimaryKey(new ProductDetailDO(productDetail));
        return productDetail;

    }

    @Override
    public ProductDetail get(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        ProductDetailDO productDetailDO = productDetailDAO.selectByPrimaryKey(id);
        if (productDetailDO == null) {
            return null;
        }
        return productDetailDO.convertToModel();
    }

    @Override
    public List<ProductDetail> getByProductId(String productId) {

        List<ProductDetail> productDetails = new ArrayList<>();
        if (StringUtils.isBlank(productId)) {
            return productDetails;
        }

        List<ProductDetailDO> productDetailDOS = productDetailDAO.selectByProductId(productId);
        if (CollectionUtils.isEmpty(productDetailDOS)) {
            return productDetails;
        }

        for (ProductDetailDO productDetailDO : productDetailDOS) {
            productDetails.add(productDetailDO.convertToModel());
        }

        return productDetails;
    }

}
