package com.ryantsui.cache;

import com.ryantsui.entity.Db;
import com.ryantsui.utils.WebContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DBCache {
    private static final Logger logger = LoggerFactory.getLogger(DBCache.class);
    private static LinkedHashMap<String, Db> dbLinkedHashMap;
    private static DBCache instance;
    private Integer TTL = 60 * 1000;

    private DBCache(){
        dbLinkedHashMap = new LinkedHashMap<>();
    }

    /**
     * 获取实例.
     * @return dbcache obj
     */
    public static synchronized DBCache getInstance() {
        if (null == instance) {
            synchronized (DBCache.class) {
                if (null == instance) {
                    instance = new DBCache();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化数据库属性并放入threadlocal
     * @param driver 驱动名称
     * @param url 链接
     * @param username 用户名
     * @param password 密码
     */
    public void initConnection(String driver, String url, String username, String password)
        throws ClassNotFoundException,SQLException{
        Db db = dbLinkedHashMap.get(WebContextUtil.getRemoteAddr());
        if (null != db) {
            if (url.equals(db.getUrl()) && username.equals(db.getUsername())) {
                db.setTtl(System.currentTimeMillis() + TTL);
                return;
            }
            db.getConnection().close();
        }
        try {
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url, username, password);
            db = new Db();
            db.setDriver(driver);
            db.setUrl(url);
            db.setUsername(username);
            db.setPassword(password);
            db.setConnection(connection);
            // 10分钟过期时间
            db.setTtl(System.currentTimeMillis() + TTL);
            dbLinkedHashMap.put(WebContextUtil.getRemoteAddr(), db);
        } catch (SQLException e) {
            logger.error("SQL异常",e);
            throw e;
        } catch (ClassNotFoundException e2) {
            logger.error("未找到数据库驱动类",e2);
            throw e2;
        }
    }

    /**
     * 获取数据库配置信息.
     * @return db
     */
    public Db getDbEntity() {
        return dbLinkedHashMap.get(WebContextUtil.getRemoteAddr());
    }

    /**
     * 获取数据库连接.
     * @return connection
     */
    public Connection getConnection (){
        return dbLinkedHashMap.get(WebContextUtil.getRemoteAddr()).getConnection();
    }

    /**
     * 关闭connection.
     */
    public void closeConnection() {
       List<String> keyList = new ArrayList<>(5);
       Db db;
       for (Map.Entry<String, Db> map: dbLinkedHashMap.entrySet()) {
            db = map.getValue();
            if (System.currentTimeMillis() > db.getTtl()) {
                try {
                    if (null != db.getConnection()) {
                        db.getConnection().close();
                    }
                } catch (SQLException e) {
                    logger.error("关闭来自{}，用户名：{}，的数据库链接失败", map.getKey(), db.getUsername());
                }
                keyList.add(map.getKey());
            }
       }
       for (String key:keyList) {
           logger.info("移除来自{}，用户名：{}，的数据库链接", key, dbLinkedHashMap.get(key).getUsername());
           dbLinkedHashMap.remove(key);
       }
    }
}
