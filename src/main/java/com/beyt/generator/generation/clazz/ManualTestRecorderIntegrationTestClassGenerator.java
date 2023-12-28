package com.beyt.generator.generation.clazz;

import com.beyt.generator.annotation.IntegrationTestGenerator;
import com.beyt.generator.domain.enumeration.ITemplateVariableEnum;
import com.beyt.generator.domain.enumeration.eIntegrationTestMethodVariable;
import com.beyt.generator.domain.enumeration.eIntegrationTestVariable;
import com.beyt.generator.generation.method.IntegrationTestMethodGenerator;
import com.beyt.generator.generation.method.selector.IntegrationTestMethodGeneratorSelector;
import com.beyt.generator.generation.method.selector.OnlyGenericIntegrationTestMethodGeneratorSelector;
import com.beyt.generator.generation.parameter.MethodGenerationParameter;
import com.beyt.generator.generation.parameter.RestMethodRecordGenerationParameter;
import com.beyt.generator.helper.IntegrationTestTemplateHelper;
import com.beyt.generator.util.ExceptionUtil;
import com.beyt.generator.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.method.HandlerMethod;

import javax.persistence.EntityManager;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class ManualTestRecorderIntegrationTestClassGenerator implements IntegrationTestClassGenerator {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private IntegrationTestMethodGeneratorSelector integrationTestMethodGeneratorSelector;
    private final IntegrationTestGenerator integrationTestGenerator;
    private final String resourceClass;

    private Map<Class<?>, String> autowireClassMap = new HashMap<>();
    private Set<Class<?>> importClasses = new HashSet<>();
    Map<String, Integer> methodNameUniquenessMap = new HashMap<>();
    private final Map<eIntegrationTestVariable, StringBuilder> variableMap = new HashMap<>();

    public ManualTestRecorderIntegrationTestClassGenerator(String className, IntegrationTestGenerator integrationTestGenerator) {
        this.integrationTestGenerator = integrationTestGenerator;
        this.resourceClass = className;
        this.integrationTestMethodGeneratorSelector = new OnlyGenericIntegrationTestMethodGeneratorSelector(integrationTestGenerator);

        for (eIntegrationTestVariable integrationTestVariable : eIntegrationTestVariable.values()) {
            variableMap.put(integrationTestVariable, new StringBuilder());
        }
    }

    @Override
    public void start() {
        fillDefaultVariables(integrationTestGenerator, importClasses, autowireClassMap);
        variableMap.get(eIntegrationTestVariable.CLASSNAME).append(resourceClass);
    }

    private void fillDefaultVariables(com.beyt.generator.annotation.IntegrationTestGenerator integrationTestGenerator, Set<Class<?>> importClasses, Map<Class<?>, String> autowireClassMap) {
        importClasses.add(integrationTestGenerator.mainClass());
        autowireClassMap.put(EntityManager.class, generateAutowireStr(EntityManager.class));
        variableMap.get(eIntegrationTestVariable.PACKAGE).append(integrationTestGenerator.packageForTest());
        variableMap.get(eIntegrationTestVariable.SPRING_START_CLASS).append(integrationTestGenerator.mainClass().getSimpleName());
        variableMap.get(eIntegrationTestVariable.TODAY).append(DATE_FORMAT.format(new Date()));
    }

    @Override
    public void append(MethodGenerationParameter param) {
        if (!(param instanceof RestMethodRecordGenerationParameter parameter)) {
            throw new IllegalStateException("parameter must be RestMethodRecordGenerationParameter");
        }
        variableMap.get(eIntegrationTestVariable.CLASS_BODY).append(generateTestMethod(parameter));
    }

    public String generateTestMethod(RestMethodRecordGenerationParameter parameter) {
        HandlerMethod handlerMethod = parameter.getHandlerMethod();
        Map<eIntegrationTestMethodVariable, String> templateValueMap = new HashMap<>();
        String methodNamePostFix = prepareMethodNamePostfix(methodNameUniquenessMap, parameter.getHandlerMethod());
        IntegrationTestMethodGenerator.Result result = null;
        String generationResult;
        templateValueMap.put(eIntegrationTestMethodVariable.MethodName, handlerMethod.getMethod().getName() + methodNamePostFix);
        parameter.setGenerationVariableMap(templateValueMap);
        // Create Method (EntityDTO dto)
        try {
            result = integrationTestMethodGeneratorSelector.select(parameter).generate(parameter);
            importClasses.addAll(result.imports());
            templateValueMap.putAll(result.requiredVariables());

        } catch (Exception e) {
            templateValueMap.put(eIntegrationTestMethodVariable.WarningMessage, "Warn: While Create Method generation exception thrown!: " + e.getMessage() + " " + ExceptionUtil.getPrintStackTrace(e));
        }

        if (Objects.isNull(result) || StringUtils.isBlank(result.generation())) {
            if (!templateValueMap.containsKey(eIntegrationTestMethodVariable.WarningMessage)) {
                templateValueMap.put(eIntegrationTestMethodVariable.WarningMessage, "Warn: Available method generate Type not found!");
            }
            generationResult = IntegrationTestTemplateHelper.generateTemplate("/integration_test/methods/EmptyITMethod.tempjava", (Map<ITemplateVariableEnum, CharSequence>) (Map<?, ?>) templateValueMap);
        } else {
            generationResult = result.generation().toString();
        }

        return generationResult;
    }

    private String prepareMethodNamePostfix(Map<String, Integer> methodNameUniquenessMap, HandlerMethod handlerMethod) {
        methodNameUniquenessMap.computeIfPresent(handlerMethod.getMethod().getName(), (key, value) -> ++value);
        methodNameUniquenessMap.putIfAbsent(handlerMethod.getMethod().getName(), 0);
        Integer integer = methodNameUniquenessMap.get(handlerMethod.getMethod().getName());
        return integer == 0 ? "" : integer.toString();
    }

    @Override
    public CharSequence generate() {
        importClasses.addAll(autowireClassMap.keySet());
        importClasses.forEach(c -> variableMap.get(eIntegrationTestVariable.IMPORTS).append("import ").append(c.getName()).append(";\n"));
        IntegrationTestTemplateHelper.generateAutowireVariable(autowireClassMap, variableMap);

        String result = IntegrationTestTemplateHelper.generateTemplate("/integration_test/IntegrationTest.tempjava", (Map<ITemplateVariableEnum, CharSequence>) (Map<?, ?>) variableMap);
        IntegrationTestTemplateHelper.writeFile(result, resourceClass + "IT.java", integrationTestGenerator.outputPath());
        log.info(result);
        log.info("END " + resourceClass);
        return result;
    }

    private String generateAutowireStr(Class<?> objectWithType) {
        Assert.notNull(objectWithType, "Class must not be null");
        String simpleName = objectWithType.getSimpleName();
        return "    @Autowired\n    " + simpleName + " " + StringUtil.lowerFirstLetter(simpleName) + ";";
    }
}
