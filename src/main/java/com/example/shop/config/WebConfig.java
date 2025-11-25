package com.example.shop.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${api.v1}")
    String api;

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping(api+"/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .exposedHeaders("Custom-Header")
                .allowCredentials(true).maxAge(3600);
        WebMvcConfigurer.super.addCorsMappings(registry);
    }
}
