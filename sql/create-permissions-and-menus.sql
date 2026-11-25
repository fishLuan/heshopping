-- 在已有 role、user_role 表的基础上增加权限和动态路由配置。
-- 可重复执行，不重置已有授权、菜单配置或停用状态。
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限主键',
    code VARCHAR(100) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '唯一权限码',
    name VARCHAR(100) NOT NULL COMMENT '权限显示名称',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作权限';

CREATE TABLE IF NOT EXISTS role_permission (
    role_id BIGINT NOT NULL COMMENT '角色主键',
    permission_id BIGINT NOT NULL COMMENT '权限主键',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permission(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色操作权限关联';

CREATE TABLE IF NOT EXISTS menu (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单主键',
    parent_id BIGINT NULL COMMENT '父菜单，顶层为空',
    name VARCHAR(100) NOT NULL COMMENT '菜单显示名称',
    node_type VARCHAR(16) NOT NULL DEFAULT 'PAGE' COMMENT 'DIRECTORY目录或PAGE页面',
    route_name VARCHAR(100) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '唯一Vue路由名称',
    path VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '完整路由路径',
    component_key VARCHAR(150) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '本地组件映射标识，目录可为空',
    permission_code VARCHAR(100) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '页面要求的权限码，无要求时为空',
    icon VARCHAR(64) NULL COMMENT '图标映射标识',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '同级排序，小值优先',
    visible BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否显示在导航菜单',
    enabled BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否下发路由，默认关闭',
    PRIMARY KEY (id),
    UNIQUE KEY uk_menu_route_name (route_name),
    UNIQUE KEY uk_menu_path (path),
    KEY idx_menu_parent_sort (parent_id, sort_order),
    CONSTRAINT fk_menu_parent FOREIGN KEY (parent_id) REFERENCES menu(id) ON DELETE RESTRICT,
    CONSTRAINT fk_menu_permission FOREIGN KEY (permission_code) REFERENCES permission(code) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态菜单和路由配置';

CREATE TABLE IF NOT EXISTS role_menu (
    role_id BIGINT NOT NULL COMMENT '角色主键',
    menu_id BIGINT NOT NULL COMMENT '菜单主键',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '菜单授权时间',
    PRIMARY KEY (role_id, menu_id),
    CONSTRAINT fk_rm_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    CONSTRAINT fk_rm_menu FOREIGN KEY (menu_id) REFERENCES menu(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联';

-- 种子数据在一个事务中写入。已有分配保持原样。
START TRANSACTION;
INSERT INTO role(code, name, enabled) VALUES
('CUSTOMER', '顾客', TRUE), ('MERCHANT', '商家', TRUE), ('ADMIN', '管理员', TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO permission(code, name) VALUES
('account:read', '查看本人账号'),
('order:create', '创建购买订单'),
('order:read-own', '查看本人购买订单'),
('order:cancel-own', '取消本人未支付订单'),
('product:manage-own', '管理本人店铺商品'),
('order:read-shop', '查看本人店铺销售订单'),
('order:ship', '处理本人店铺发货'),
('user:read', '查询平台用户'),
('user:disable', '禁用账号'),
('merchant:review', '审核商家申请'),
('role:assign', '分配用户角色'),
('order:read-all', '查看平台全部订单')
ON DUPLICATE KEY UPDATE id = permission.id;

INSERT IGNORE INTO role_permission(role_id, permission_id)
SELECT r.id, p.id FROM role r JOIN permission p ON
    (p.code = 'account:read' AND r.code IN ('CUSTOMER','MERCHANT','ADMIN'))
    OR (r.code = 'CUSTOMER' AND p.code IN ('order:create','order:read-own','order:cancel-own'))
    OR (r.code = 'MERCHANT' AND p.code IN ('product:manage-own','order:read-shop','order:ship'))
    OR (r.code = 'ADMIN' AND p.code IN ('user:read','user:disable','merchant:review','role:assign','order:read-all'));

-- 仅录入已有页面。组件标识对应现有Vue页面，业务页面后续开发时再增加。
INSERT INTO menu(name, route_name, path, component_key, permission_code, sort_order, enabled) VALUES
('我的账号', 'account', '/account', 'account', 'account:read', 10, TRUE),
('用户管理', 'admin-users', '/admin/users', 'admin/users', 'user:read', 20, TRUE)
ON DUPLICATE KEY UPDATE id = menu.id;

INSERT IGNORE INTO role_menu(role_id, menu_id)
SELECT r.id, m.id FROM role r JOIN menu m ON
    (m.route_name = 'account' AND r.code IN ('CUSTOMER','MERCHANT','ADMIN'))
    OR (m.route_name = 'admin-users' AND r.code = 'ADMIN');
COMMIT;
