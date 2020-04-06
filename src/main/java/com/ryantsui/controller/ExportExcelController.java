package com.ryantsui.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryantsui.cache.DBCache;
import com.ryantsui.entity.JsonMessage;
import com.ryantsui.service.DBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ExportExcelController {
    private static final Logger logger = LoggerFactory.getLogger(Excel2DBController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    private static final int eachPageRow = 5000;
    private final DBService dbService;
    @Autowired
    public ExportExcelController(DBService dbService) {
        this.dbService = dbService;
    }



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
        Connection connection = DBCache.getInstance().getConnection();
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
        String originSql = stringBuilder.toString();
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("下载数据", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

            Map<String, List<List<String>>> data;
            List<List<String>> headList = new ArrayList<>();
            List<List<String>> dataList;
            int totalRows = queryRows;
            if (0 == queryRows) {
                totalRows = dbService.countTotalRows(originSql, connection);
            }
            dataList = new ArrayList<>(totalRows*2);
            for (int page = 1; page <= (totalRows/eachPageRow + 1); page++) {
                data = dbService.list(getPageSql(originSql, page, totalRows), connection);
                dataList.addAll(data.get("list"));
                logger.info("共计 {} 行, 已完成 {} 行", totalRows, dataList.size());
                if (1 == page) {
                    headList = data.get("head");
                }
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
            logger.error("下载失败：{}", e.getMessage());
            // 重置response
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().println(objectMapper.writeValueAsString(
                    new JsonMessage().failure("下载失败:" + e.getMessage())));
        }
    }

    /**
     * 拼装分页sql
     * @param sql sql
     * @param page 页码
     * @return string
     */
    private String getPageSql(String sql, int page, int total) {
        StringBuilder stringBuilder = new StringBuilder();
        String driver = DBCache.getInstance().getDbEntity().getDriver();
        int startRowNum = (page -1) * eachPageRow;
        int endRowNum = page * eachPageRow;
        if (driver.contains("mysql")) {
            stringBuilder.append(sql).append(" limit ").append(startRowNum).append(",");
            if (endRowNum > total) {
                stringBuilder.append(total - startRowNum);
            } else {
                stringBuilder.append(eachPageRow);
            }
        } else if (driver.contains("oracle")) {
            sql = sql.replace("select ", "select ROWNUM r,");
            stringBuilder.append(" SELECT * FROM (").append(sql);
            if (!sql.contains("where")) {
                stringBuilder.append(" where ");
            }
            // total+1 避免最后一条数据查询不出来问题
            stringBuilder.append(" rownum < ").append(Math.min(endRowNum, total + 1));
            stringBuilder.append(") t where t.r >=").append(startRowNum);
        }
        return stringBuilder.toString();
    }
}
