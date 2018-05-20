package com.ryantsui.config;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库配置信息单例模式.
 * Created by xufy on 2018/5/19.
 */
public class DBConfig {
    private static DBConfig instance = null;
    private static Map<String,Object> map = null;

    private DBConfig() {
        map = new HashMap<String,Object>();
    }

    /**
     * 获取实例.
     * @return DBConfig
     */
    public static DBConfig getInstance() {
        if (null == instance) {
            instance = new DBConfig();
        }
        return instance;
    }

    /**
     * 添加数据.
     * @param key 键
     * @param value 值
     */
    public void put(String key,Object value) {
        map.put(key,value);
    }

    /**
     * 获取数据.
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return map.get(key);
    }
    /**
     * 清空数据.
     */
    public static void clear () {
        if (null != map) {
            map.clear();
        }
    }
}
