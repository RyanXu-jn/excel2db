package com.ryantsui.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryantsui.utils.DbUtil;
import com.ryantsui.entity.JsonMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 控制类.
 * Created by xufy on 2018/5/8.
 */
@Controller
@RequestMapping("file")
@Slf4j
public class Excel2DBController {
    private ObjectMapper objectMapper;

    public Excel2DBController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 用户自定义数据库连接.
     * @param driver 驱动名称
     * @param url 地址
     * @param username 用户名
     * @param password 密码
     * @return JsonMessage
     * @throws SQLException 异常
     */
    @PostMapping(value = "/listDBTables")
    @ResponseBody
    public JsonMessage connectDataBase(@RequestParam(value = "driver") String driver,
                                       @RequestParam(value = "url") String url,
                                       @RequestParam(value = "username") String username,
                                       @RequestParam(value = "password") String password)
            throws SQLException {
        DbUtil.initConnection(driver, url, username, password);
        List<String> tableList = new ArrayList<>();
        Connection conn = DbUtil.getConnection();
        DatabaseMetaData databaseMetaData = conn.getMetaData();
        ResultSet rs = databaseMetaData.getTables(conn.getCatalog(), conn.getSchema(),
                "%", new String[]{"TABLE"});
        while (rs.next()) {
            tableList.add(rs.getString("TABLE_NAME"));
        }
        return new JsonMessage().success(tableList);
    }


    /**
     * 获取数据库某个表的所有字段.
     * @param tableName 表名
     * @return JsonMessage
     */
    @GetMapping(value="listTableColumns")
    @ResponseBody
    public JsonMessage listTableColumns(@RequestParam(value="tableName") String tableName) {
        if (!StringUtils.isNotBlank(tableName)) {
            return new JsonMessage().failure("表名不能为空");
        }
        DSLContext dslContext = DbUtil.getDSLContext();
        Result<Record> resultRecord = dslContext.selectFrom(tableName).where("1=2").fetch();
        Field[] fields = resultRecord.fields();
        List<String> list = new ArrayList<>(fields.length * 2);
        for (int i = 0; i < fields.length; i++) {
            list.add(fields[i].getName());
        }
        return new JsonMessage().success(list);
    }

    /**
     * 创建新表.
     * @param tableName 表名
     * @param data 字段数据
     * @return JsonMessage
     */
    @PostMapping(value="createNewTable")
    @ResponseBody
    public JsonMessage createNewTable(String tableName,String data) throws IOException {
        DSLContext context = DbUtil.getDSLContext();
        List<Map<String,String>> columnList = objectMapper.readValue(data,new TypeReference<List<Map<String,String>>>(){});
        Field<?>[] fields = new Field[columnList.size()];
        Map<String,String> tempMap;
        for (int i = 0; i < columnList.size(); i++) {
            tempMap = columnList.get(i);
            fields[i] = DSL.field(tempMap.get("name"),
                    DefaultDataType.getDataType(DbUtil.getSQLDialet(), tempMap.get("type")));
        }
        context.createTable(tableName).columns(fields).execute();
        return new JsonMessage().success();
    }

    /**
     *  插入数据到数据库.
     * @param tableName 表名
     * @param columns 列名
     * @param data 数据
     * @return JsonMessage
     * @throws ClassNotFoundException 异常
     * @throws SQLException 异常
     */
    @RequestMapping(value = "saveData")
    @ResponseBody
    public JsonMessage saveData(String tableName,String columns,String data,String columnTypes) throws Exception {
        List<List<String>> dataList = objectMapper.readValue(data, new TypeReference< List<List<String>>>() {});
        String[] columnsArr = columns.split(",");
        if (columnsArr.length != dataList.get(0).size()) {
            return new JsonMessage().failure("列数不匹配！");
        }
        DSLContext context = DbUtil.getDSLContext();
        Field<?>[] fields = context.selectFrom(tableName).fetch().fields();
        List<Field<?>> comparedFields = new ArrayList<>(fields.length);
        for (int i = 0; i < fields.length; i++) {
            if (columns.toLowerCase().indexOf(fields[i].getName().toLowerCase()) > -1) {
                comparedFields.add(fields[i]);
            }
        }
        Field<?>[] tmpFields = new Field[comparedFields.size()];
        comparedFields.toArray(tmpFields);
        //50条数据进行一次提交
        int num = 50;
        if (dataList.size() <= num) {
            saveDataIns(context, tableName, tmpFields, dataList);
        } else {
            int fromIndex = 0,toIndex = num;
            while(true) {
                if (toIndex > dataList.size()) {
                    toIndex = dataList.size();
                }
                saveDataIns(context, tableName, tmpFields, dataList.subList(fromIndex,toIndex));
                if (toIndex == dataList.size()) {
                    break;
                }
                fromIndex = toIndex;
                toIndex += num;
            }
        }
        return new JsonMessage().success();
    }
    private void saveDataIns(DSLContext context, String tableName,Field<?>[] tmpFields,List<List<String>> dataList) {
        BatchBindStep step = context.batch(context.insertInto(DSL.table(tableName), tmpFields).values(
                Arrays.stream(tmpFields).map(e -> "?").collect(Collectors.toList())
        ));
        dataList.forEach(e -> {
            step.bind(e.toArray());
        });
        StopWatch stopWatch = new StopWatch("批量插入");
        stopWatch.start();
        step.execute();
        stopWatch.stop();
        log.info("数据总量:{},{}", dataList.size(), stopWatch.prettyPrint());
    }
}
