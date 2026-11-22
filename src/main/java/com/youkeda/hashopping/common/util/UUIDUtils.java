package com.youkeda.hashopping.common.util;

import java.util.UUID;


/**
 * 通用响应、分页与工具：UUIDUtils。
 */
public class UUIDUtils {
    public static final String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
