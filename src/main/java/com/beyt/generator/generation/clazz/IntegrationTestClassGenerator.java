package com.beyt.generator.generation.clazz;

import com.beyt.generator.generation.parameter.MethodGenerationParameter;

public interface IntegrationTestClassGenerator {
    void start();

    void append(MethodGenerationParameter methodGenerationParameter);

    CharSequence generate();
}
