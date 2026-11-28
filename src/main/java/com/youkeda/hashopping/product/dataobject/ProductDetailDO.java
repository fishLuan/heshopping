package com.youkeda.hashopping.product.dataobject;

import com.youkeda.hashopping.product.model.ProductDetail;
import org.springframework.beans.BeanUtils;

import java.util.Date;

/**
 * 商品及商品详情管理：ProductDetailDO。
 */
public class ProductDetailDO {
    /**
     * 主键
     */
    private String id;

    /** 商家定义的款式名称，例如白色、白色加绒款。 */
    private String style;

    /** 获取款式名称。 */
    public String getStyle() {
        return style;
    }

    /** 设置款式名称。 */
    public void setStyle(String style) {
        this.style = style;
    }

    /**
     * 关联商品
     */
    private String productId;

    /**
     * 价格
     */
    private Double price;

    /**
     * 尺码
     */
    private Double size;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 创建时间
     */
    private Date gmtCreated;

    /**
     * 修改时间
     */
    private Date gmtModified;

    /**
     * 获取主键
     */
    public String getId() {
        return id;
    }

    /**
     * 设置主键
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取关联商品
     */
    public String getProductId() {
        return productId;
    }

    /**
     * 设置关联商品
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * 获取价格
     */
    public Double getPrice() {
        return price;
    }

    /**
     * 设置价格
     */
    public void setPrice(Double price) {
        this.price = price;
    }

    /**
     * 获取尺码
     */
    public Double getSize() {
        return size;
    }

    /**
     * 设置尺码
     */
    public void setSize(Double size) {
        this.size = size;
    }

    /**
     * 获取库存
     */
    public Integer getStock() {
        return stock;
    }

    /**
     * 设置库存
     */
    public void setStock(Integer stock) {
        this.stock = stock;
    }

    /**
     * 获取创建时间
     */
    public Date getGmtCreated() {
        return gmtCreated;
    }

    /**
     * 设置创建时间
     */
    public void setGmtCreated(Date gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    /**
     * 获取修改时间
     */
    public Date getGmtModified() {
        return gmtModified;
    }

    /**
     * 设置修改时间
     */
    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public ProductDetailDO() {

    }

    public ProductDetailDO(ProductDetail productDetail) {

        BeanUtils.copyProperties(productDetail, this);

    }

    public ProductDetail convertToModel() {
        ProductDetail productDetail = new ProductDetail();
        BeanUtils.copyProperties(this, productDetail);
        return productDetail;
    }
}