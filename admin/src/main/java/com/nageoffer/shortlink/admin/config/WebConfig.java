package com.nageoffer.shortlink.admin.config;

import com.nageoffer.shortlink.admin.common.biz.user.UserInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserInterceptor userInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/shortlink/v1/user/login",
                        "/api/shortlink/v1/user/register",
                        "/api/shortlink/v1/user/has-username"
                );
    }
}