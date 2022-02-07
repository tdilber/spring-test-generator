package com.beyt.generator.util;

import com.beyt.generator.exception.NoAnnotationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tdilber at 6/25/2020
 */
@Slf4j
public class ApplicationContextUtil {
    public static <T extends Annotation> T getFirstAnnotation(ApplicationContext applicationContext, Class<T> annotation) {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(annotation);

        if (beansWithAnnotation.isEmpty()) {
            throw new NoAnnotationException(annotation.getSimpleName() + " annotation not found!");
        }

        String beanName = beansWithAnnotation.keySet().iterator().next();

        T annotationOnBean = applicationContext.findAnnotationOnBean(beanName, annotation);

        if (annotationOnBean == null) {
            throw new NoAnnotationException(annotation.getSimpleName() + " annotation object not found!");
        }

        return annotationOnBean;
    }

    public static <T extends Annotation> List<T> getAllAnnotations(ApplicationContext applicationContext, Class<T> annotation) {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(annotation);

        if (beansWithAnnotation.isEmpty()) {
            throw new NoAnnotationException(annotation.getSimpleName() + " annotation not found!");
        }

        List<T> annotations = new ArrayList<>();
        for (String beanName : beansWithAnnotation.keySet()) {
            annotations.add(applicationContext.findAnnotationOnBean(beanName, annotation));
        }

        if (annotations.isEmpty()) {
            throw new NoAnnotationException(annotation.getSimpleName() + " annotation object not found!");
        }

        return annotations;
    }

    public static <T extends Annotation> Map<String, T> getAllAnnotationsMap(ApplicationContext applicationContext, Class<T> annotation) {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(annotation);

        if (beansWithAnnotation.isEmpty()) {
            throw new NoAnnotationException(annotation.getSimpleName() + " annotation not found!");
        }

        Map<String, T> annotationsMap = new HashMap<>();
        for (String beanName : beansWithAnnotation.keySet()) {
            annotationsMap.put(beanName, applicationContext.findAnnotationOnBean(beanName, annotation));
        }

        if (annotationsMap.isEmpty()) {
            throw new NoAnnotationException(annotation.getSimpleName() + " annotation object not found!");
        }

        return annotationsMap;
    }
}
