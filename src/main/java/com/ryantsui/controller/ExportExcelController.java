package com.ryantsui.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryantsui.config.DBConfig;
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

    private DBService dbService;
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
    public void downloadFailedUsingJson(HttpServletResponse response, @RequestBody Map<String, String> param) throws IOException {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            if ("custom".equalsIgnoreCase(param.get("type"))) {
                stringBuilder.append(param.get("sql"));
            } else {
                stringBuilder.append("select ").append(param.get("columnName")).append(" from ")
                        .append(param.get("tableName"));
                if (!"All".equals(param.get("dataRows"))) {
                    String driver = (String) DBConfig.getInstance().get("driver");
                    if (driver.contains("mysql")) {
                        stringBuilder.append(" limit ").append(param.get("dataRows"));
                    } else if (driver.contains("oracle")) {
                        stringBuilder.append(" where rownum < ").append(param.get("dataRows"));
                    }
                }
            }
            Map<String, Object> data = dbService.list(stringBuilder.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("下载数据", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream()).head((List<List<String>>)data.get("head"))
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("Sheet1").doWrite((List<List<String>>)data.get("list"));
            // 这里需要设置不关闭流
            //EasyExcel.write(response.getOutputStream(), DownloadData.class).autoCloseStream(Boolean.FALSE).sheet("模板")
                    //.doWrite(data());
        } catch (Exception e) {
            // 重置response
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().println(objectMapper.writeValueAsString(new JsonMessage().failure("下载失败:" + e.getMessage())));
        }
    }
}
