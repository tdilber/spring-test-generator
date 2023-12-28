package com.beyt.generator.generation.method.value;

import com.beyt.generator.generation.parameter.MethodGenerationParameter;

public interface MethodValuesProvider {
    Result generate(MethodGenerationParameter methodGenerationParameter);

    public record Result(Object returnValue, Object[] methodParameters) {

    }
}
