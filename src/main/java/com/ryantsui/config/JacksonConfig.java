package com.ryantsui.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /**
     * 实例化jackson objectMapper对象.
     * @return objectMapper实例
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper() .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }
}
