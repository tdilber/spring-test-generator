package com.beyt.generator.generation.test;

import com.beyt.generator.generation.parameter.MethodGenerationParameter;

public interface IntegrationGenerator {
    Type type();

    void start();

    void append(MethodGenerationParameter methodGenerationParameter);

    CharSequence generate();

    public enum Type {
        MANUAL_TEST_RECORDER,
        ALL_METHODS_WITH_RANDOM_VALUES
    }
}
