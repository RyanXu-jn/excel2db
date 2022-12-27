package com.ryantsui.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.ryantsui.entity.DbInfo;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.util.ObjectUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DbUtil {
    private static Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();
    private static Map<String, DbInfo> dbInfoMap = new ConcurrentHashMap<>();
    private static Integer maxPoolSize = 20;
    private static Integer minPoolSize = 5;


    /**
     * 初始化数据库属性并放入threadlocal
     * @param driver 驱动名称
     * @param url 链接
     * @param username 用户名
     * @param password 密码
     */
    public static void initConnection(String driver,String url,String username,
                                      String password) {
        DataSource dataSource = dataSourceMap.get(WebContextUtil.getRemoteAddr());
        if (!ObjectUtils.isEmpty(dataSource)) {
           return;
        }
        DbInfo dbInfo = new DbInfo(driver, url, username, password);
        SQLDialect sqlDialect = SQLDialect.MYSQL;
        if (driver.contains("oracle")) {
            sqlDialect = SQLDialect.ORACLE;
        } else if (driver.contains("postgre")) {
            sqlDialect = SQLDialect.POSTGRES;
        }
        dbInfo.setSqlDialect(sqlDialect);
        dbInfoMap.put(WebContextUtil.getRemoteAddr(), dbInfo);
        DruidDataSource ds = new DruidDataSource();
        ds.setDriverClassName(driver);
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setMinIdle(minPoolSize);
        ds.setMaxActive(maxPoolSize);
        try {
            ds.init();
            dataSourceMap.put(WebContextUtil.getRemoteAddr(), ds);
        } catch (SQLException e) {
            log.error("数据源初始化失败",e);
            throw new RuntimeException("数据源初始化失败",e);
        }
    }

    /**
     * 获取数据库连接.
     * @return connection
     */
    public static Connection getConnection (){
        try {
            return dataSourceMap.get(WebContextUtil.getRemoteAddr()).getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取joop上下文信息
     * @return context
     */
    public static DSLContext getDSLContext() {
        return DSL.using(getConnection(), getSQLDialet());
    }

    public static void close() {
        getDSLContext().close();
    }

    /**
     * 获取数据库配置信息
     * @return
     */
    public static DbInfo getDbInfo() {
        return dbInfoMap.get(WebContextUtil.getRemoteAddr());
    }

    public static SQLDialect getSQLDialet() {
        return dbInfoMap.get(WebContextUtil.getRemoteAddr()).getSqlDialect();
    }
}
