package com.ryantsui.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryantsui.entity.JsonMessage;
import com.ryantsui.utils.DbUtil;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 控制类.
 * Created by xufy on 2020/1/30.
 */
@Controller
@RequestMapping("excel")
@Slf4j
public class ExportExcelController {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    private static final int eachPageRow = 5000;



    /**
     * 文件下载（失败了会返回一个有部分数据的Excel）
     * 这里注意，finish的时候会自动关闭OutputStream,当然你外面再关闭流问题不大
     */
    @PostMapping("download")
    public void download(HttpServletResponse response) throws IOException {

        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("测试", "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        //EasyExcel.write(response.getOutputStream(), DownloadData.class).sheet("模板").doWrite(data());
    }

    /**
     * 文件下载并且失败的时候返回json（默认失败了会返回一个有部分数据的Excel）
     *
     */
    @PostMapping("downloadFailedUsingJson")
    public void downloadFailedUsingJson(HttpServletResponse response, @RequestBody Map<String, String> param)
            throws IOException {
        Connection connection = DbUtil.getConnection();
        StringBuilder stringBuilder = new StringBuilder();
        int queryRows = 0;
        if ("custom".equalsIgnoreCase(param.get("type"))) {
            stringBuilder.append(param.get("sql"));
        } else {
            stringBuilder.append("select ").append(param.get("columnName")).append(" from ")
                    .append(param.get("tableName"));
            if (!"All".equals(param.get("dataRows"))) {
                queryRows = Integer.parseInt(param.get("dataRows"));
            }
        }
        String originSql = "(".concat(stringBuilder.toString()).concat(") it");
        DSLContext context = DbUtil.getDSLContext();
        SelectWhereStep selectWhereStep = context.selectFrom(originSql);
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // String fileName = URLEncoder.encode("下载数据", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=".concat(param.get("tableName")).concat(".xlsx"));

            List<List<String>> headList = new ArrayList<>();
            int totalRows = queryRows;
            if (0 == queryRows) {
                Result<Record1<Integer>> result = context.selectCount().from(originSql).fetch();
                totalRows = (int) (result.get(0)).get(0);
            }
            List<List<String>> dataList = new ArrayList<>(totalRows*2);
            Result<Record> queryResult;
            for (int page = 0; page < (totalRows/eachPageRow + 1); page++) {
                queryResult = selectWhereStep.limit(eachPageRow).offset(page * eachPageRow).fetch();
                if (0 == page) {
                    Field[] fields = queryResult.fields();
                    List<String> columnList;
                    for (int i = 0; i < fields.length; i++) {
                        columnList = new ArrayList<>(5);
                        columnList.add(fields[i].getName());
                        headList.add(columnList);
                    }
                }
                for (Record record: queryResult) {
                    List<String> rowList = new ArrayList<>(queryResult.fields().length * 2);
                    for (List<String> head: headList) {
                        rowList.add(String.valueOf(record.getValue(head.get(0))));
                    }
                    dataList.add(rowList);
                }
                log.info("共计 {} 行, 已完成 {} 行", totalRows, dataList.size());
            }
            EasyExcel.write(response.getOutputStream()).head(headList)
                  .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("Sheet1").doWrite(dataList);
            /* v1.0.0
            // 实例化excelWriter
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream())
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).build();
            WriteSheet writeSheet = EasyExcel.writerSheet("Sheet1").build();
            // 查询全部数据处理步骤
            int totalRows = queryRows;
            if (0 == queryRows) {
                totalRows = dbService.countTotalRows(originSql, connection);
            }
            for (int page = 1; page < (totalRows/eachPageRow + 1); page++) {
                data = dbService.list(getPageSql(originSql, page, totalRows), connection);
                excelWriter.write(data.get("list"), writeSheet);
                logger.info("共计 {} 行, 已完成 {} 行", totalRows, page*eachPageRow);
                if (1 == page) {
                    writeSheet.setHead(data.get("head"));
                }
            }*/
            //1. EasyExcel.write(response.getOutputStream()).head((List<List<String>>)data.get("head"))
              //      .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                //    .sheet("Sheet1").doWrite((List<List<String>>)data.get("list"));
            // 这里需要设置不关闭流
            //2. EasyExcel.write(response.getOutputStream(), DownloadData.class).autoCloseStream(Boolean.FALSE).sheet("模板")
                    //.doWrite(data());
        } catch (Exception e) {
            log.error("下载失败：", e);
            // 重置response
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().println(objectMapper.writeValueAsString(
                    new JsonMessage().failure("下载失败:" + e.getMessage())));
        }
    }
}
