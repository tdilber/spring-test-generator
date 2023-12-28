package com.beyt.generator.generation.method.selector;

import com.beyt.generator.annotation.IntegrationTestGenerator;
import com.beyt.generator.generation.method.GenericIntegrationTestMethodGenerator;
import com.beyt.generator.generation.method.IntegrationTestMethodGenerator;
import com.beyt.generator.generation.parameter.MethodGenerationParameter;


public class OnlyGenericIntegrationTestMethodGeneratorSelector implements IntegrationTestMethodGeneratorSelector {
    private final IntegrationTestMethodGenerator integrationTestMethodGenerator;
    private final IntegrationTestGenerator integrationTestGenerator;

    public OnlyGenericIntegrationTestMethodGeneratorSelector(IntegrationTestGenerator integrationTestGenerator) {
        this.integrationTestGenerator = integrationTestGenerator;
        this.integrationTestMethodGenerator = new GenericIntegrationTestMethodGenerator(this.integrationTestGenerator);
    }

    @Override
    public IntegrationTestMethodGenerator select(MethodGenerationParameter methodGenerationParameter) {
        return integrationTestMethodGenerator;
    }
}
