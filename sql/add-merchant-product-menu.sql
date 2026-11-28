SET NAMES utf8mb4;
START TRANSACTION;
INSERT INTO menu(name,route_name,path,component_key,permission_code,sort_order,visible,enabled)
VALUES('添加商品','merchant-add-product','/merchant/products/add','merchant/products/add','product:manage-own',30,TRUE,TRUE)
ON DUPLICATE KEY UPDATE name=VALUES(name),component_key=VALUES(component_key),permission_code=VALUES(permission_code),visible=TRUE,enabled=TRUE;
INSERT IGNORE INTO role_menu(role_id,menu_id)
SELECT r.id,m.id FROM role r JOIN menu m ON m.route_name='merchant-add-product'
WHERE r.code='MERCHANT';
COMMIT;
