package com.bowerzlabs.kraftpulse.springboot.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
public class KraftPulseWebConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/kraft/pulse/**")
                .addResourceLocations("classpath:/META-INF/resources/kraftpulse/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/kraft/pulse", "/kraft/pulse/");

        registry
                .addViewController("/kraft/pulse/")
                .setViewName("forward:/kraft/pulse/index.html");
    }
}

