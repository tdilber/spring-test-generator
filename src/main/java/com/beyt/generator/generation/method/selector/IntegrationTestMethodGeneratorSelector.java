package com.beyt.generator.generation.method.selector;

import com.beyt.generator.generation.method.IntegrationTestMethodGenerator;
import com.beyt.generator.generation.parameter.MethodGenerationParameter;

public interface IntegrationTestMethodGeneratorSelector {
    IntegrationTestMethodGenerator select(MethodGenerationParameter methodGenerationParameter);
}
