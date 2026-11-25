package com.youkeda.hashopping.product.model;

import java.util.Date;

/**
 * 商品及商品详情管理：Product。
 */
public class Product {

    /**
     * 主键
     */
    private String id;

    /** 商品所属店铺主键。 */
    private Long shopId;

    /** 查询时关联的店铺名称。 */
    private String shopName;

    /** 获取商品所属店铺。 */
    public Long getShopId() {
        return shopId;
    }

    /** 设置商品所属店铺，业务层应校验经营权限。 */
    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    /** 获取店铺展示名称。 */
    public String getShopName() {
        return shopName;
    }

    /** 设置查询得到的店铺展示名称。 */
    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品默认展示的价格
     */
    private Double price;

    /**
     * 商品销量
     */
    private Integer purchaseNum;

    /**
     * 商品介绍
     */
    private String productIntro;

    /**
     * 商品图片
     */
    private String productImgs;

    /**
     * 创建时间
     */
    private Date gmtCreated;

    /**
     * 创建日期
     */
    private Date gmtModified;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getPurchaseNum() {
        return purchaseNum;
    }

    public void setPurchaseNum(Integer purchaseNum) {
        this.purchaseNum = purchaseNum;
    }

    public String getProductIntro() {
        return productIntro;
    }

    public void setProductIntro(String productIntro) {
        this.productIntro = productIntro;
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

    public String getProductImgs() {
        return productImgs;
    }

    public void setProductImgs(String productImgs) {
        this.productImgs = productImgs;
    }
}
