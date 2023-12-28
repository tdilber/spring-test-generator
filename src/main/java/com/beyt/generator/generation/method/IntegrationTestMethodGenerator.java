package com.beyt.generator.generation.method;

import com.beyt.generator.domain.enumeration.eIntegrationTestMethodVariable;
import com.beyt.generator.generation.parameter.MethodGenerationParameter;

import java.util.Map;
import java.util.Set;

public interface IntegrationTestMethodGenerator {
    Result generate(MethodGenerationParameter methodGenerationParameter);

    public record Result(CharSequence generation, Set<Class<?>> imports,
                         Map<eIntegrationTestMethodVariable, String> requiredVariables) {
    }
}
