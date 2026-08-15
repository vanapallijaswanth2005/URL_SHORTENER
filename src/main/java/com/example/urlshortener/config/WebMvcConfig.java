package com.example.urlshortener.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitingInterceptor rateLimitingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // We only rate limit the URL creation endpoint to prevent spam.
        // We do not rate limit the redirects themselves.
        registry.addInterceptor(rateLimitingInterceptor)
                .addPathPatterns("/api/urls/shorten");
    }
}
