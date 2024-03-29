package com.beyt.generator.generation.test;


import com.beyt.generator.generation.clazz.IntegrationTestClassGenerator;
import com.beyt.generator.generation.clazz.RandomValuesIntegrationTestClassGenerator;
import com.beyt.generator.generation.parameter.MethodGenerationParameter;

public class RandomValuesIntegrationGenerator implements IntegrationGenerator {
    private final IntegrationTestClassGenerator integrationTestClassGenerator;

    public RandomValuesIntegrationGenerator() {
        this.integrationTestClassGenerator = new RandomValuesIntegrationTestClassGenerator();
    }

    @Override
    public Type type() {
        return Type.TEST_WITH_RANDOM_DATA_GENERATOR;
    }

    @Override
    public void start() {

    }

    @Override
    public void append(MethodGenerationParameter methodGenerationParameter) {

    }

    @Override
    public CharSequence generate() {
        return null;
    }
}
