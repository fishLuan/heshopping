-- 历史订单允许快照为空，新订单由业务服务完整写入。
ALTER TABLE `order`
 ADD COLUMN quantity INT NOT NULL DEFAULT 1 COMMENT '购买数量',
 ADD COLUMN unit_price DECIMAL(18,2) NULL COMMENT '成交单价',
 ADD COLUMN shop_id BIGINT NULL COMMENT '成交店铺',
 ADD COLUMN product_name VARCHAR(64) NULL COMMENT '商品名称快照',
 ADD COLUMN sku_snapshot VARCHAR(256) NULL COMMENT '规格快照',
 ADD COLUMN request_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '幂等标识',
 MODIFY COLUMN total_price DECIMAL(18,2) NOT NULL,
 ADD UNIQUE KEY uk_order_user_request (user_id,request_id);
