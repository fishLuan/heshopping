CREATE TABLE cart_item (
 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '购物车条目',
 user_id BIGINT NOT NULL COMMENT '所属用户',
 sku_id VARCHAR(32) NOT NULL COMMENT '商品规格，失效后仍保留供用户清理',
 quantity INT NOT NULL COMMENT '加购数量',
 gmt_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY(id),
 UNIQUE KEY uk_cart_user_sku(user_id,sku_id),
 CONSTRAINT fk_cart_user FOREIGN KEY(user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户购物车条目';
SET NAMES utf8mb4;
INSERT INTO permission(code,name,enabled) VALUES('cart:manage-own','管理本人购物车',TRUE)
ON DUPLICATE KEY UPDATE id=permission.id;
INSERT IGNORE INTO role_permission(role_id,permission_id)
SELECT r.id,p.id FROM role r JOIN permission p ON p.code='cart:manage-own' WHERE r.code='CUSTOMER';
INSERT INTO menu(name,route_name,path,component_key,permission_code,sort_order,enabled)
VALUES('购物车','my-cart','/cart','account/cart','cart:manage-own',16,TRUE)
ON DUPLICATE KEY UPDATE id=menu.id;
INSERT IGNORE INTO role_menu(role_id,menu_id)
SELECT r.id,m.id FROM role r JOIN menu m ON m.route_name='my-cart' WHERE r.code='CUSTOMER';
