package com.youkeda.hashopping.product.service;

import com.youkeda.hashopping.product.param.*;
import com.youkeda.hashopping.product.model.CreateProductResponse;
import com.youkeda.hashopping.product.dataobject.*;
import com.youkeda.hashopping.product.dao.*;
import com.youkeda.hashopping.shop.service.ShopService;
import com.youkeda.hashopping.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/** 创建商品及全部规格，任何校验或写入失败都会回滚。 */
@Service
public class ProductCreationService {
    private final ProductDAO products;
    private final ProductDetailDAO skus;
    private final ShopService shops;
    /** 注入商品、规格持久层和店铺服务。 */
    public ProductCreationService(ProductDAO products,ProductDetailDAO skus,ShopService shops) {
        this.products=products; this.skus=skus; this.shops=shops;
    }
    /** 验证输入和店铺归属后，在同一事务中创建全部数据。 */
    @Transactional
    public CreateProductResponse create(long userId,CreateProductRequest request) {
        if(request==null || request.shopId==null || request.shopId<1) fail("请选择店铺");
        String name=text(request.name,64,"商品名称");
        String intro=request.productIntro==null?"":textOptional(request.productIntro,256,"商品介绍");
        if(request.skus==null || request.skus.isEmpty() || request.skus.size()>200) fail("每个商品需要1至200个SKU");
        if(request.productImgs==null || request.productImgs.isEmpty() || request.productImgs.size()>9) fail("请提供1至9张图片地址");
        List<String> images=request.productImgs.stream().map(this::image).collect(Collectors.toList());
        String imageList=String.join(",",images);
        if(imageList.length()>1024) fail("图片地址总长度不能超过1024");
        Set<String> combinations=new HashSet<>();
        List<ProductDetailDO> rows=new ArrayList<>();
        BigDecimal lowest=null;
        for(CreateSkuRequest input:request.skus) {
            if(input==null) fail("SKU不能为空");
            String style=text(input.style,64,"款式");
            BigDecimal size=decimal(input.size,"尺码",new BigDecimal("999999.99"));
            BigDecimal price=decimal(input.price,"价格",new BigDecimal("99999999.99"));
            if(input.stock==null || input.stock<0) fail("库存必须是非负整数");
            // 与数据库当前不区分大小写的款式规则保持一致，最终唯一约束再次兜底。
            String key=style.toLowerCase(Locale.ROOT)+"|"+size.toPlainString();
            if(!combinations.add(key)) fail("同一商品不能重复提交款式与尺码组合");
            ProductDetailDO row=new ProductDetailDO();
            row.setId(id());
            row.setStyle(style);
            row.setSize(size.doubleValue());
            row.setPrice(price.doubleValue());
            row.setStock(input.stock);
            rows.add(row);
            if(lowest==null || price.compareTo(lowest)<0) lowest=price;
        }
        shops.requireOwnedActive(userId,request.shopId);
        ProductDO product=new ProductDO();
        product.setId(id());
        product.setShopId(request.shopId);
        product.setName(name);
        product.setProductIntro(intro);
        product.setProductImgs(imageList);
        product.setPrice(lowest.doubleValue());
        product.setPurchaseNum(0);
        products.insert(product);
        CreateProductResponse response=new CreateProductResponse();
        response.productId=product.getId();
        try {
            for(ProductDetailDO row:rows) {
                row.setProductId(product.getId());
                skus.insert(row);
                response.skuIds.add(row.getId());
            }
        } catch(org.springframework.dao.DuplicateKeyException ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,"DUPLICATE_SKU","存在重复的款式与尺码组合");
        }
        return response;
    }
    /** 校验正数及两位小数，拒绝静默截断价格或尺码。 */
    private BigDecimal decimal(BigDecimal value,String label,BigDecimal max) {
        if(value==null || value.signum()<=0 || value.compareTo(max)>0) fail(label+"超出允许范围");
        try { return value.setScale(2,RoundingMode.UNNECESSARY); }
        catch(ArithmeticException ex) { throw new BusinessException(HttpStatus.BAD_REQUEST,"INVALID_ARGUMENT",label+"最多两位小数"); }
    }
    /** 图片只允许站内绝对路径或HTTP(S)地址，逗号不允许进入分隔存储。 */
    private String image(String value) {
        String url=text(value,1024,"图片地址");
        if(url.contains(",") || url.indexOf((char)92)>=0 || url.chars().anyMatch(Character::isISOControl)) fail("图片地址不合法");
        try {
            URI uri=URI.create(url);
            boolean local=url.startsWith("/") && !url.startsWith("//");
            boolean remote=("https".equals(uri.getScheme()) || "http".equals(uri.getScheme())) && uri.getHost()!=null;
            if(!local && !remote) fail("图片只支持站内路径或HTTP(S)地址");
        } catch(IllegalArgumentException ex) { fail("图片地址不合法"); }
        return url;
    }
    /** 校验必填文本。 */
    private String text(String value,int max,String label) {
        if(value==null || value.trim().isEmpty()) fail(label+"不能为空");
        return textOptional(value,max,label);
    }
    /** 清理首尾空格并校验长度。 */
    private String textOptional(String value,int max,String label) {
        String result=value.trim();
        if(result.length()>max) fail(label+"不能超过"+max+"个字符");
        return result;
    }
    /** 生成服务端商品或SKU主键。 */
    private String id() { return UUID.randomUUID().toString().replace("-",""); }
    /** 返回统一参数错误。 */
    private void fail(String message) { throw new BusinessException(HttpStatus.BAD_REQUEST,"INVALID_ARGUMENT",message); }
}
