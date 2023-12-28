package com.beyt.generator.configuration;

import com.beyt.generator.generation.method.value.MethodReturnIgnoreGenericConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

@Configuration
public class MethodReturnIgnoreGenericConverterDefaults {
    @Bean
    public <T> MethodReturnIgnoreGenericConverter<ResponseEntity<T>, T> responseEntityConverter() {
        return new MethodReturnIgnoreGenericConverter<>() {
            @Override
            public Class<?> getGenericType() {
                return ResponseEntity.class;
            }

            @Override
            public T convert(ResponseEntity<T> x) {
                return x.getBody();
            }
        };
    }
}
