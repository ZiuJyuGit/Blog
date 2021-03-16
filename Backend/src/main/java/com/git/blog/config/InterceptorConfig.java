package com.git.blog.config;

import com.git.blog.interceptor.CrosInterceptor;
import com.git.blog.interceptor.JWTInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final CrosInterceptor crosInterceptor;

    private final JWTInterceptor jwtInterceptor;

    public InterceptorConfig(CrosInterceptor crosInterceptor, JWTInterceptor jwtInterceptor) {
        this.crosInterceptor = crosInterceptor;
        this.jwtInterceptor = jwtInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(crosInterceptor)
                .addPathPatterns("/**");
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login");
    }
}
