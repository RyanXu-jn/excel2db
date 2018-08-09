package com.ryantsui.service;

import com.ryantsui.config.DBConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by xufy on 2018/5/19.
 */
@Service
public class Excel2DBService {
    private static final Logger logger = LoggerFactory.getLogger(Excel2DBService.class);
    private String[] pattern = new String[]{"yyyy-MM", "yyyyMM", "yyyy/MM",
            "yyyyMMdd", "yyyy-MM-dd", "yyyy/MM/dd",
            "yyyyMMddHHmmss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss"};
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
    public void saveDataIns(String tableName,String columns,List<List<String>> dataList,List<Map<String,String>> columnTypeList)
            throws ClassNotFoundException, SQLException, ParseException {
        try {
            connection = this.getConnection();
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            StringBuffer stringBuffer;
            List<String> subList = null;
            String columnType = null;
            for (List<String> aDataList : dataList) {
                subList = aDataList;
                stringBuffer = new StringBuffer();
                stringBuffer.append("insert into ").append(tableName).append(" (").append(columns).append(") ");
                stringBuffer.append("values (");
                Date date;
                for (int j = 0; j < subList.size(); j++) {
                    columnType = columnTypeList.get(j).get("type");
                    if ("INT".equals(columnType) || "FLOAT".equals(columnType) || "DOUBLE".equals(columnType)
                            || "NUMBER".equals(columnType) || "LONG".equals(columnType)) {
                        if (StringUtils.isNotBlank(subList.get(j))) {
                            stringBuffer.append(subList.get(j));
                        } else {
                            stringBuffer.append(0);
                        }
                    } else if ("DATE".equals(columnType)) {
                        date = DateUtils.parseDate(subList.get(j),pattern);
                        stringBuffer.append("'").append(dateFormat.format(date)).append("'");
                    } else if ("TIMESTAMP".equals(columnType)) {
                        date = DateUtils.parseDate(subList.get(j),pattern);
                        stringBuffer.append("'").append(timestampFormat.format(date)).append("'");
                    } else {
                        stringBuffer.append("'").append(subList.get(j)).append("'");
                    }
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
        } catch (SQLException | ParseException e) {
            logger.error("SQL异常",e);
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
