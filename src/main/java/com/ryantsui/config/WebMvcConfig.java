package com.ryantsui.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 页面映射配置.
 * Created by xufy on 2018/8/9.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 注册页面控制器.
     * @param registry 注册对象
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index.html");
        registry.addViewController("/index").setViewName("index.html");
        registry.addViewController("/file/file").setViewName("file/file.html");
        registry.addViewController("/url/url").setViewName("url/url.html");
        registry.addViewController("/exportFile/index").setViewName("exportFile/index.html");
    }
}
