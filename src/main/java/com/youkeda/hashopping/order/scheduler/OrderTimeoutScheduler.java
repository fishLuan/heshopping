package com.youkeda.hashopping.order.scheduler;

import com.youkeda.hashopping.order.dao.OrderCloseDAO;
import com.youkeda.hashopping.order.service.OrderCloseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 每轮分批关闭到期订单，单笔失败记录日志并在后续轮次重试。 */
@Component
@EnableScheduling
public class OrderTimeoutScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(OrderTimeoutScheduler.class);
    private final OrderCloseDAO orders;
    private final OrderCloseService closer;

    /** 注入到期查询和独立事务关单服务。 */
    public OrderTimeoutScheduler(OrderCloseDAO orders, OrderCloseService closer) {
        this.orders = orders;
        this.closer = closer;
    }

    /** 默认每30秒扫描最多100笔，多实例重复扫描由订单锁保证幂等。 */
    @Scheduled(fixedDelayString = "${hashopping.order.timeout-scan-ms:30000}")
    public void scan() {
        for (String number : orders.findExpired()) {
            try {
                closer.closeExpired(number);
            } catch (Exception ex) {
                LOG.error("超时关单失败，订单号={}", number, ex);
            }
        }
    }
}
