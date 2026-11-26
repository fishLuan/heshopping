SET NAMES utf8mb4;
INSERT INTO menu(name,route_name,path,component_key,permission_code,sort_order,enabled)
VALUES('我的订单','my-orders','/orders','account/orders','order:read-own',15,TRUE)
ON DUPLICATE KEY UPDATE id=menu.id;
INSERT IGNORE INTO role_menu(role_id,menu_id)
SELECT r.id,m.id FROM role r JOIN menu m ON m.route_name='my-orders' WHERE r.code='CUSTOMER';
