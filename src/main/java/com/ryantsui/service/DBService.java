package com.ryantsui.service;

import com.ryantsui.cache.DBCache;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by xufy on 2018/5/19.
 */
@Service
public class DBService {
    private static final Logger logger = LoggerFactory.getLogger(DBService.class);
    private String[] pattern = new String[]{"yyyy-MM", "yyyyMM", "yyyy/MM",
            "yyyyMMdd", "yyyy-MM-dd", "yyyy/MM/dd",
            "yyyyMMddHHmmss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss"};
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取数据库下所有数据表.
     * @return 表列表
     * @throws SQLException 异常
     */
    public List<String> listAllTables(Connection connection)
            throws SQLException {
        List<String> list = new ArrayList<String>();
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        String username = DBCache.getInstance().getDbEntity().getUsername();
        ResultSet rs = databaseMetaData.getTables(null, username.toUpperCase(Locale.getDefault()), "%", new String[]{"TABLE"});
        while (rs.next()) {
            list.add(rs.getString("TABLE_NAME"));
        }
        return list;
    }

    /**
     * 获取表所有列名.
     * @param sql 查询语句
     * @return List<String>
     * @throws SQLException 异常
     */
    public List<String> listTableAllColumns(String sql, Connection connection) throws SQLException{
        List<String> list = new ArrayList<>();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        ResultSetMetaData rs = resultSet.getMetaData();
        for (int i = 0; i < rs.getColumnCount(); i++) {
            list.add(rs.getColumnName(i + 1));
        }
        resultSet.close();
        preparedStatement.close();
        return list;
    }

    /**
     * 创建新表.
     * @param sql sql语句
     * @throws SQLException 异常
     */
    public void createNewTable(String sql, Connection connection) throws SQLException{
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    /**
     * 统计查询总数.
     * @param sql sql
     * @param connection 数据库连接
     * @return int
     * @throws SQLException 异常
     */
    public int countTotalRows(String sql,Connection connection) throws SQLException {
        String countSql = "select count(1) from (" + sql + ") t";
        PreparedStatement preparedStatement = connection.prepareStatement(countSql);
        ResultSet resultSet = preparedStatement.executeQuery();
        int totalRows = 0;
        if (resultSet.next()) {
            totalRows = resultSet.getInt(1);
        }
        return totalRows;
    }

    /**
     * 根据sql查询结果.
     * @param sql sql
     * @return list
     * @throws SQLException 异常
     */
    public Map<String, List<List<String>>> list(String sql, Connection connection) throws SQLException {
        PreparedStatement  preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        ResultSetMetaData md = resultSet.getMetaData(); //获得结果集结构信息,元数据
        int columnCount = md.getColumnCount();   //获得列数
        //拼装head
        List<List<String>> head = new ArrayList<>();
        List<String> headN;
        for (int i = 1; i <= columnCount; i++) {
            headN = new ArrayList<>();
            headN.add(md.getColumnName(i));
            head.add(headN);
        }
        //拼装list
        List<List<String>> list = new ArrayList<>();
        List<String> rowData;
        while (resultSet.next()) {
            rowData = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                rowData.add(String.valueOf(resultSet.getObject(i)));
            }
            list.add(rowData);
        }
        resultSet.close();
        preparedStatement.close();
        Map<String, List<List<String>>> data = new HashMap<>();
        data.put("list", list);
        data.put("head", head);
        return data;
    }

    /**
     * 插入到数据库实例.
     * @param tableName 表名
     * @param columns 列名
     * @param dataList 数据
     * @throws ClassNotFoundException 异常
     * @throws SQLException 异常
     */
    public void saveDataIns(String tableName,String columns,List<List<String>> dataList,
                            List<Map<String,String>> columnTypeList, Connection connection)
            throws Exception {
        connection.setAutoCommit(false);
        String driverName = DBCache.getInstance().getDbEntity().getDriver();
        String sql = null;
        if (driverName.contains("mysql")) {
            sql = batchInsertMysql(tableName, columns, dataList, columnTypeList);
        } else if (driverName.contains("oracle")) {
            sql = batchInsertOracle(tableName, columns, dataList, columnTypeList);
        } else {
            throw new Exception("driver field is null");
        }
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();
        connection.commit();
        connection.setAutoCommit(true);
    }
    private String batchInsertOracle(String tableName,String columns,List<List<String>> dataList,
                                     List<Map<String,String>> columnTypeList) throws ParseException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("insert into ").append(tableName).append(" (").append(columns).append(") ");
        stringBuilder.append(" (");
        List<String> subList = null;
        for (int i = 0; i < dataList.size(); i++) {
            subList = dataList.get(i);
            stringBuilder.append(" select ");
            stringBuilder.append(assembleFiled(subList, columnTypeList));
            stringBuilder.append(" from dual ");
            if (i != dataList.size() - 1) {
                stringBuilder.append(" union all ");
            }
        }
        stringBuilder.append(" )");
        return stringBuilder.toString();

    }
    private String batchInsertMysql(String tableName, String columns, List<List<String>> dataList,
                                    List<Map<String,String>> columnTypeList) throws ParseException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("insert into ").append(tableName).append(" (").append(columns).append(") ");
        stringBuilder.append("values ");
        List<String> subList = null;
        for (int i = 0; i < dataList.size(); i++) {
            subList = dataList.get(i);
            stringBuilder.append(" (");
            stringBuilder.append(assembleFiled(subList, columnTypeList));
            stringBuilder.append(" )");
            if (i != dataList.size() - 1) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }
    private String assembleFiled(List<String> subList,
                                 List<Map<String,String>> columnTypeList) throws ParseException {
        StringBuilder result = new StringBuilder();
        Date date;
        String columnType;
        String columnValue;
        for (int j = 0; j < subList.size(); j++) {
            columnType = columnTypeList.get(j).get("type");
            columnValue = subList.get(j);
            if ("INT".equals(columnType) || "FLOAT".equals(columnType) || "DOUBLE".equals(columnType)
                    || "NUMBER".equals(columnType) || "LONG".equals(columnType)) {
                if (StringUtils.isNotBlank(columnValue)) {
                    result.append(columnValue);
                } else {
                    result.append(0);
                }
            } else if ("DATE".equals(columnType)) {
                date = DateUtils.parseDate(columnValue,pattern);
                result.append("'").append(dateFormat.format(date)).append("'");
            } else if ("TIMESTAMP".equals(columnType)) {
                date = DateUtils.parseDate(columnValue,pattern);
                result.append("'").append(timestampFormat.format(date)).append("'");
            } else {
                result.append("'").append(columnValue).append("'");
            }
            if (j != subList.size() - 1) {
                result.append(",");
            }
        }
        return result.toString();
    }
}
