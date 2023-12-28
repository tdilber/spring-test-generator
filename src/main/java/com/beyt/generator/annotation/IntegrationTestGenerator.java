package com.beyt.generator.annotation;

import com.beyt.generator.aspect.LiveTestGenerateAspect;
import com.beyt.generator.aspect.LiveTestGenerateContext;
import com.beyt.generator.aspect.WebConfig;
import com.beyt.generator.configuration.AopConfig;
import com.beyt.generator.generation.IntegrationTestGenerateManager;
import com.beyt.generator.generation.test.IntegrationGenerator;
import com.beyt.generator.helper.IntegrationTestMethodGenerator;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by tdilber at 7/2/2020
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Import({com.beyt.generator.manager.IntegrationTestGenerator.class, IntegrationTestMethodGenerator.class, AopConfig.class, LiveTestGenerateAspect.class, IntegrationTestGenerateManager.class, WebConfig.class, LiveTestGenerateContext.class})
public @interface IntegrationTestGenerator {
    String packageForTest();

    Class<?> mainClass();

    String outputPath();

    String[] recordedRequestHeaders() default {};

    IntegrationGenerator.Type generationType() default IntegrationGenerator.Type.MANUAL_TEST_RECORDER;

    boolean deleteGenerationDirectory() default false;

    Class<?>[] ignoreClasses() default {BasicErrorController.class};

    Class<?>[] ignoreMethodReturnGeneric() default {ResponseEntity.class};

    Class<?>[] ignoreMethodArgTypes() default {HttpServletRequest.class, HttpServletResponse.class};
}
