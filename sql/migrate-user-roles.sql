-- 先复制现有授权，再移除用户表中的旧角色字段。
CREATE TABLE IF NOT EXISTS `role` (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES `role` (id) ON DELETE CASCADE
) ENGINE=InnoDB;

INSERT INTO `role` (code, name) VALUES ('CUSTOMER', '顾客'), ('ADMIN', '管理员')
ON DUPLICATE KEY UPDATE id = `role`.id;

INSERT IGNORE INTO `role` (code, name)
SELECT DISTINCT u.role, u.role FROM `user` u WHERE u.role IS NOT NULL AND u.role <> '';

INSERT IGNORE INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM `user` u JOIN `role` r ON r.code = u.role;

ALTER TABLE `user` DROP COLUMN role;
