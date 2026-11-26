SET NAMES utf8mb4;
INSERT INTO permission(code,name,enabled) VALUES('address:manage-own','管理本人收货地址',TRUE)
ON DUPLICATE KEY UPDATE id=permission.id;
INSERT IGNORE INTO role_permission(role_id,permission_id)
SELECT r.id,p.id FROM role r JOIN permission p ON p.code='address:manage-own' WHERE r.code='CUSTOMER';
INSERT INTO menu(name,route_name,path,component_key,permission_code,sort_order,enabled)
VALUES('收货地址','my-addresses','/addresses','account/addresses','address:manage-own',18,TRUE)
ON DUPLICATE KEY UPDATE id=menu.id;
INSERT IGNORE INTO role_menu(role_id,menu_id)
SELECT r.id,m.id FROM role r JOIN menu m ON m.route_name='my-addresses' WHERE r.code='CUSTOMER';
