package com.beyt.generator.aspect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("integration-test-generator")
public class WebConfig implements WebMvcConfigurer {

    private LiveTestGenerateInterceptor liveTestGenerateInterceptor;

    @Autowired
    public void setLiveTestGenerateInterceptor(LiveTestGenerateInterceptor liveTestGenerateInterceptor) {
        this.liveTestGenerateInterceptor = liveTestGenerateInterceptor;
    }

    @Bean
    public LiveTestGenerateInterceptor liveTestGenerateInterceptor() {
        return new LiveTestGenerateInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(liveTestGenerateInterceptor);
    }
}
