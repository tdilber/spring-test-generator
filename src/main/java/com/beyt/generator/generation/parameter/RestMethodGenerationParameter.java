package com.beyt.generator.generation.parameter;

import com.beyt.generator.domain.enumeration.eIntegrationTestMethodVariable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.util.Map;

public class RestMethodGenerationParameter extends MethodGenerationParameter {
    @Getter
    @Setter
    protected RequestMappingInfo requestMappingInfo;

    @Getter
    @Setter
    protected HandlerMethod handlerMethod;


    @Getter
    @Setter
    protected Map<eIntegrationTestMethodVariable, String> generationVariableMap;
}
