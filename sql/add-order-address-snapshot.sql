ALTER TABLE `order`
 ADD COLUMN address_id BIGINT NULL COMMENT '下单选择的地址标识，删除地址不影响快照',
 ADD COLUMN recipient_name VARCHAR(50) NULL COMMENT '收货人快照',
 ADD COLUMN recipient_phone VARCHAR(32) NULL COMMENT '收货电话快照',
 ADD COLUMN shipping_address VARCHAR(500) NULL COMMENT '完整收货地址快照';
