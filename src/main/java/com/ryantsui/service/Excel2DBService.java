package com.ryantsui.service;

import com.ryantsui.config.DBConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by xufy on 2018/5/19.
 */
@Service
public class Excel2DBService {
    private static final Logger logger = LoggerFactory.getLogger(Excel2DBService.class);
    private Connection connection = null;

    /**
     * 获取数据库下所有数据表.
     * @return 表列表
     * @throws ClassNotFoundException 异常
     * @throws SQLException 异常
     */
    public List<String> listAllTables()
            throws ClassNotFoundException,SQLException {
        List<String> list = new ArrayList<String>();
        try {
            connection = this.getConnection();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            String username = (String)DBConfig.getInstance().get("username");
            ResultSet rs = databaseMetaData.getTables(null, username.toUpperCase(Locale.getDefault()), "%", new String[]{"TABLE"});
            while (rs.next()) {
                list.add(rs.getString("TABLE_NAME"));
            }
            this.closeConnection();
        } catch (SQLException e) {
            throw e;
        }
        return list;
    }

    /**
     * 获取表所有列名.
     * @param sql 查询语句
     * @return List<String>
     * @throws ClassNotFoundException 异常
     * @throws SQLException 异常
     */
    public List<String> listTableAllColumns(String sql) throws ClassNotFoundException,SQLException{
        List<String> list = new ArrayList<String>();
        try {
            connection = this.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData rs = resultSet.getMetaData();
            for (int i = 0; i < rs.getColumnCount(); i++) {
                list.add(rs.getColumnName(i + 1));
            }
            connection.close();
        } catch (SQLException e) {
            throw e;
        }
        return list;
    }

    /**
     * 创建新表.
     * @param sql sql语句
     * @throws ClassNotFoundException 异常
     * @throws SQLException 异常
     */
    public void createNewTable(String sql) throws ClassNotFoundException,SQLException{
        try {
            connection = this.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            throw e;
        }
    }
    /**
     * 插入到数据库实例.
     * @param tableName 表名
     * @param columns 列名
     * @param dataList 数据
     * @throws ClassNotFoundException 异常
     * @throws SQLException 异常
     */
    public void saveDataIns(String tableName,String columns,List<List<String>> dataList)
            throws ClassNotFoundException,SQLException{
        try {
            connection = this.getConnection();
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            StringBuffer stringBuffer;
            List<String> subList = null;
            for (int i = 0; i < dataList.size(); i++) {
                subList = dataList.get(i);
                stringBuffer = new StringBuffer();
                stringBuffer.append("insert into ").append(tableName).append(" (").append(columns).append(") ");
                stringBuffer.append("values (");
                for (int j = 0; j < subList.size(); j++) {
                    stringBuffer.append("'" + subList.get(j) + "'");
                    if (j != subList.size() - 1) {
                        stringBuffer.append(",");
                    }
                }
                stringBuffer.append(" )");
                statement.addBatch(stringBuffer.toString());
            }
            statement.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
            this.closeConnection();
        } catch (SQLException e) {
            logger.error("SQL异常");
            throw e;
        }
    }
    /**
     * 获取数据库连接.
     * @return Connection
     * @throws ClassNotFoundException 异常
     * @throws SQLException 异常
     */
    private Connection getConnection()
            throws ClassNotFoundException,SQLException {
        try {
            String driver = (String)DBConfig.getInstance().get("driver");
            String url = (String)DBConfig.getInstance().get("url");
            String username = (String)DBConfig.getInstance().get("username");
            String password = (String)DBConfig.getInstance().get("password");
            Class.forName(driver);
            connection = DriverManager.getConnection(url,username,password);
        } catch (SQLException e) {
            logger.error("SQL异常",e);
            throw e;
        } catch (ClassNotFoundException e2) {
            logger.error("未找到数据库驱动类",e2);
            throw e2;
        }
        return connection;
    }

    /**
     * 关闭数据库连接.
     * @throws SQLException 异常
     */
    private void closeConnection() throws SQLException {
        try {
            if (null != connection) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("关闭数据库连接失败",e);
            throw e;
        }
    }
}
