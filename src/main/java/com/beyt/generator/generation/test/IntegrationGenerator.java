package com.beyt.generator.generation.test;

import com.beyt.generator.generation.parameter.MethodGenerationParameter;

public interface IntegrationGenerator {
    Type type();

    void start();

    void append(MethodGenerationParameter methodGenerationParameter);

    CharSequence generate();

    enum Type {
        LIVE_TEST_RECORDER,
        TEST_WITH_RANDOM_DATA_GENERATOR
    }
}
