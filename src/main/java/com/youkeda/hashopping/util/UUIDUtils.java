package com.youkeda.hashopping.util;

import java.util.UUID;


public class UUIDUtils {
    public static final String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
