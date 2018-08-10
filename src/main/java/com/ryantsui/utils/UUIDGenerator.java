package com.ryantsui.utils;

import java.util.Locale;
import java.util.UUID;

/**
 *  UUID生成器.
 * Created by xufy on 2018/4/3.
 */
public class UUIDGenerator {

    /**
     * 获取UUID字符串.
     * @return UUID字符串
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().toUpperCase(Locale.CHINESE).replaceAll("-","");
    }

    public static void main(String [] args) {
        for (int i = 0; i < 4; i++) {
            System.out.println(getUUID());
        }
    }
}
