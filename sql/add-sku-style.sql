SET NAMES utf8mb4;
ALTER TABLE product_detail
 ADD COLUMN style VARCHAR(64) NOT NULL DEFAULT '标准款' COMMENT '商家自定义款式名称',
 ADD UNIQUE KEY uk_product_style_size(product_id,style,size);
