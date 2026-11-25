-- 本地演示数据迁移：执行前确认 user2 已拥有启用的 MERCHANT 角色。
SET NAMES utf8mb4;
CREATE TABLE shop (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '店铺主键',
    owner_user_id BIGINT NOT NULL COMMENT '经营者用户主键',
    name VARCHAR(100) NOT NULL COMMENT '店铺名称',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE营业、DISABLED停用',
    gmt_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_shop_owner (owner_user_id),
    CONSTRAINT fk_shop_owner FOREIGN KEY (owner_user_id) REFERENCES user(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺与经营者归属';

ALTER TABLE product ADD COLUMN shop_id BIGINT NULL COMMENT '所属店铺',
    ADD KEY idx_product_shop (shop_id),
    ADD CONSTRAINT fk_product_shop FOREIGN KEY (shop_id) REFERENCES shop(id) ON DELETE RESTRICT;

START TRANSACTION;
INSERT INTO shop(owner_user_id,name)
SELECT u.id,'Hashopping 演示鞋履店' FROM user u
WHERE u.user_name='user2' AND u.status='ACTIVE'
AND EXISTS (SELECT 1 FROM user_role ur JOIN role r ON r.id=ur.role_id
            WHERE ur.user_id=u.id AND r.code='MERCHANT' AND r.enabled=TRUE);
UPDATE product p JOIN shop s ON s.name='Hashopping 演示鞋履店'
    JOIN user u ON u.id=s.owner_user_id AND u.user_name='user2'
SET p.shop_id=s.id WHERE p.id LIKE 'hsdemo%' AND p.shop_id IS NULL;
COMMIT;
-- 所有商品回填完成后约束为必填，防止新增无归属商品。
ALTER TABLE product MODIFY COLUMN shop_id BIGINT NOT NULL COMMENT '所属店铺';
