package com.beyt.generator.annotation;

import com.beyt.generator.helper.IntegrationTestMethodGenerator;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by tdilber at 7/2/2020
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Import({com.beyt.generator.manager.IntegrationTestGenerator.class, IntegrationTestMethodGenerator.class})
public @interface IntegrationTestGenerator {
    String packageForTest();

    Class<?> mainClass();

    String outputPath();

    boolean deleteGenerationDirectory() default false;

    Class<?>[] ignoreClasses() default {BasicErrorController.class};

    Class<?>[] ignoreMethodReturnGeneric() default {ResponseEntity.class};
}
