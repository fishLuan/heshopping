package com.youkeda.hashopping.order.service;

import com.youkeda.hashopping.order.dao.OrderCreationDAO;
import com.youkeda.hashopping.order.model.CreateOrderResponse;
import com.youkeda.hashopping.order.param.CreateOrderRequest;
import com.youkeda.hashopping.product.model.PurchasableSku;
import com.youkeda.hashopping.product.service.StockService;
import com.youkeda.hashopping.user.service.BuyerService;
import com.youkeda.hashopping.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

/** 组织单SKU下单、幂等检查和库存预占事务。 */
@Service
public class OrderCreationService {
    @org.springframework.beans.factory.annotation.Value("${hashopping.order.payment-timeout-minutes:15}")
    private long timeoutMinutes;

    private final OrderCreationDAO orders;
    @org.springframework.beans.factory.annotation.Autowired
    private com.youkeda.hashopping.address.service.AddressService addresses;

    private final StockService stock;
    private final BuyerService buyers;

    /** 注入订单持久层及跨模块服务。 */
    public OrderCreationService(OrderCreationDAO orders, StockService stock, BuyerService buyers) {
        this.orders = orders;
        this.stock = stock;
        this.buyers = buyers;
    }

    /** 同一次请求只创建一笔订单、扣一次库存，价格完全由服务端计算。 */
    @Transactional
    public CreateOrderResponse create(long userId, CreateOrderRequest request) {
        if (request == null || request.skuId == null || !request.skuId.matches("[A-Za-z0-9_-]{1,32}")
                || request.quantity == null || request.quantity < 1 || request.addressId == null || request.addressId < 1
                || request.requestId == null || !request.requestId.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", "请提供有效的SKU、正整数数量和请求标识");
        }
        buyers.lockActive(userId);
        String buyerId = Long.toString(userId);
        CreateOrderResponse existing = orders.find(buyerId, request.requestId);
        if (existing != null) {
            if (!existing.skuId.equals(request.skuId) || !existing.quantity.equals(request.quantity) || !java.util.Objects.equals(existing.addressId,request.addressId)) {
                throw new BusinessException(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT", "同一请求标识不能用于不同的下单内容");
            }
            return existing;
        }
        com.youkeda.hashopping.address.model.AddressResponse address = addresses.get(userId,request.addressId);
        PurchasableSku sku = stock.reserve(request.skuId, request.quantity);
        CreateOrderResponse order = new CreateOrderResponse();
        order.addressId = address.id;
        order.recipientName = address.recipientName;
        order.recipientPhone = address.recipientPhone;
        order.shippingAddress = address.province + " " + address.city + " " + address.district + " " + address.detailAddress;
        order.id = UUID.randomUUID().toString().replace("-", "");
        order.orderNumber = order.id;
        order.userId = buyerId;
        order.skuId = request.skuId;
        order.quantity = request.quantity;
        order.unitPrice = sku.price;
        order.totalPrice = sku.price.multiply(BigDecimal.valueOf(request.quantity));
        order.shopId = sku.shopId;
        order.productName = sku.productName;
        order.skuSnapshot = sku.skuSnapshot;
        order.requestId = request.requestId;
        order.status = "WAIT_BUYER_PAY";
        order.timeoutMinutes = timeoutMinutes;
        orders.insert(order);
        return orders.find(buyerId, request.requestId);
    }
}
