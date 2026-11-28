package com.youkeda.hashopping.product.model;

import java.util.Date;


/**
 * 商品及商品详情管理：ProductDetail。
 */
public class ProductDetail {

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
     * 商品
     */
    private Product product;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getSize() {
        return size;
    }

    public void setSize(Double size) {
        this.size = size;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Date getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(Date gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
