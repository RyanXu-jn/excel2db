package com.ryantsui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by xufy on 2018/5/20.
 */
//@Controller
public class IndexController {
    private static final String INDEX = "file/index";
    /**
     * 返回文件上传首页面.
     * @return 页面
     */
    @RequestMapping(value = {"index","/"})
    public String index() {
        return INDEX;
    }
}
