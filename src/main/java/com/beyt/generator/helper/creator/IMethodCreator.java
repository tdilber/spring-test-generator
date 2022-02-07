package com.beyt.generator.helper.creator;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.util.Set;

/**
 * Created by tdilber at 11/17/2020
 */
public interface IMethodCreator {
    String name();

    String templatePath();

    boolean isAvailable(Class<?> resourceClass, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod);

    String createMethod(Class<?> resourceClass, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod, Set<Class<?>> importClasses) throws Exception;
}
