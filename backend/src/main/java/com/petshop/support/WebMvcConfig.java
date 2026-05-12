package com.petshop.support;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final CurrentUserArgumentResolver currentUserArgumentResolver;
    private final CurrentAdminArgumentResolver currentAdminArgumentResolver;

    public WebMvcConfig(CurrentUserArgumentResolver currentUserArgumentResolver,
                        CurrentAdminArgumentResolver currentAdminArgumentResolver) {
        this.currentUserArgumentResolver = currentUserArgumentResolver;
        this.currentAdminArgumentResolver = currentAdminArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
        resolvers.add(currentAdminArgumentResolver);
    }
}
