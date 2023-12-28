package com.beyt.generator.generation.parameter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;

public class RestMethodRecordGenerationParameter extends RestMethodGenerationParameter {
    @Getter
    @Setter
    protected Object returnValue;

    @Getter
    @Setter
    protected Object[] parameterArgs;

    @Getter
    @Setter
    protected Map<String, String> headerArgs;

    @Getter
    @Setter
    protected HttpServletRequest httpServletRequest;


    @Getter
    @Setter
    protected HttpServletResponse httpServletResponse;

    public static MethodGenerationParameter of(HttpServletRequest request, HttpServletResponse response, Method method, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod, Object returnValue, Object[] parameterArgs, Map<String, String> headerArgs) {
        RestMethodRecordGenerationParameter parameter = new RestMethodRecordGenerationParameter();
        parameter.setHttpServletRequest(request);
        parameter.setHttpServletResponse(response);
        parameter.setMethod(method);
        parameter.setRequestMappingInfo(requestMappingInfo);
        parameter.setHandlerMethod(handlerMethod);
        parameter.setReturnValue(returnValue);
        parameter.setParameterArgs(parameterArgs);
        parameter.setHeaderArgs(headerArgs);

        return parameter;
    }
}
