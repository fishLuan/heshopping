-- 只有新流程(request_id非空)已预扣库存，历史订单不自动释放。
ALTER TABLE `order` ADD COLUMN expires_at DATETIME NULL COMMENT '支付截止时间',
 ADD KEY idx_order_status_expiry(status,expires_at);
UPDATE `order` SET expires_at=DATE_ADD(gmt_created,INTERVAL 15 MINUTE)
 WHERE request_id IS NOT NULL AND status='WAIT_BUYER_PAY';
