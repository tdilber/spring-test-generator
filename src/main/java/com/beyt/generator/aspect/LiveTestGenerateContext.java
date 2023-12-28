package com.beyt.generator.aspect;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.io.Serializable;
import java.lang.reflect.Method;


@Setter
@Getter
@Component
@RequestScope
public class LiveTestGenerateContext implements Serializable {

    private Boolean testGenerating = null;
    private Method method = null;
    private Object returnValue = null;
    private Object[] parameterArgs = null;

    public LiveTestGenerateContext() {
    }

    public LiveTestGenerateContext(Boolean testGenerating, Method method, Object returnValue, Object[] parameterArgs) {
        this.testGenerating = testGenerating;
        this.method = method;
        this.returnValue = returnValue;
        this.parameterArgs = parameterArgs;
    }
}
