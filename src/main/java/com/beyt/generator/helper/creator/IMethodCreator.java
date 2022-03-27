package com.beyt.generator.helper.creator;

import com.beyt.generator.domain.enumeration.ITemplateVariableEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.util.Map;
import java.util.Set;

/**
 * Created by tdilber at 11/17/2020
 */
public interface IMethodCreator {
    Integer priority();

    String name();

    String templatePath();

    boolean isAvailable(Class<?> resourceClass, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod);

    String createMethod(Class<?> resourceClass, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod, Set<Class<?>> importClasses, Map<ITemplateVariableEnum, CharSequence> templateValueMap, CreateProperties createProperties) throws Exception;


    @Data
    @AllArgsConstructor
    class CreateProperties {
        private Class<?>[] ignoreMethodReturnGeneric;
    }
}
