package com.beyt.generator.generation.test;


import com.beyt.generator.annotation.IntegrationTestGenerator;
import com.beyt.generator.domain.enumeration.ITemplateVariableEnum;
import com.beyt.generator.domain.enumeration.eIntegrationTestVariable;
import com.beyt.generator.generation.clazz.IntegrationTestClassGenerator;
import com.beyt.generator.generation.clazz.ManualTestRecorderIntegrationTestClassGenerator;
import com.beyt.generator.generation.parameter.MethodGenerationParameter;
import com.beyt.generator.helper.IntegrationTestTemplateHelper;

import java.util.HashMap;
import java.util.Map;

public class ManualTestRecorderIntegrationGenerator implements IntegrationGenerator {
    private final IntegrationTestClassGenerator integrationTestClassGenerator;
    private final IntegrationTestGenerator integrationTestGenerator;

    public ManualTestRecorderIntegrationGenerator(IntegrationTestGenerator integrationTestGenerator) {
        this.integrationTestGenerator = integrationTestGenerator;
        this.integrationTestClassGenerator = new ManualTestRecorderIntegrationTestClassGenerator("SingleClass", integrationTestGenerator);
    }

    @Override
    public Type type() {
        return Type.MANUAL_TEST_RECORDER;
    }

    @Override
    public void start() {
        generateTestUtilClass(integrationTestGenerator);
        integrationTestClassGenerator.start();
    }

    @Override
    public void append(MethodGenerationParameter methodGenerationParameter) {
        integrationTestClassGenerator.append(methodGenerationParameter);
    }

    @Override
    public CharSequence generate() {
        return integrationTestClassGenerator.generate();
    }

    private void generateTestUtilClass(com.beyt.generator.annotation.IntegrationTestGenerator integrationTestGenerator) {
        Map<ITemplateVariableEnum, CharSequence> testUtilMap = new HashMap<>();
        testUtilMap.put(eIntegrationTestVariable.PACKAGE, integrationTestGenerator.packageForTest());
        String result = IntegrationTestTemplateHelper.generateTemplate("/integration_test/TestUtil.tempjava", testUtilMap);
        IntegrationTestTemplateHelper.writeFile(result, "TestUtil.java", integrationTestGenerator.outputPath());
    }
}
