package com.youkeda.hashopping.product.param;

/** 商城列表请求，页码从一开始，排序只接受固定枚举值。 */
public class ProductPageRequest {
    private int pageNum = 1;
    private int pageSize = 12;
    private String keyword;
    private String sort = "latest";

    /** 获取页码。 */
    public int getPageNum() { return pageNum; }
    /** 设置页码。 */
    public void setPageNum(int pageNum) { this.pageNum = pageNum; }
    /** 获取每页数量。 */
    public int getPageSize() { return pageSize; }
    /** 设置每页数量。 */
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    /** 获取商品名称关键词。 */
    public String getKeyword() { return keyword; }
    /** 设置商品名称关键词。 */
    public void setKeyword(String keyword) { this.keyword = keyword; }
    /** 获取排序方式，支持 latest、priceAsc、priceDesc。 */
    public String getSort() { return sort; }
    /** 设置排序方式。 */
    public void setSort(String sort) { this.sort = sort; }
    /** 计算数据库分页偏移，使用长整型避免乘法溢出。 */
    public long getOffset() { return ((long) pageNum - 1) * pageSize; }
}
