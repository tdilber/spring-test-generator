package com.beyt.generator.helper.creator;

import com.beyt.generator.domain.enumeration.ITemplateVariableEnum;
import com.beyt.generator.domain.enumeration.eIntegrationTestMethodVariable;
import com.beyt.generator.helper.IntegrationTestTemplateHelper;
import com.beyt.generator.manager.IntegrationTestGenerator;
import com.beyt.generator.util.DummyDataReflectionUtil;
import com.beyt.generator.util.field.FieldUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by tdilber at 11/17/2020
 */
@Slf4j
public class CreateEntityMethodCreator extends BaseMethodCreator {
    public CreateEntityMethodCreator(IntegrationTestGenerator integrationTestGenerator) {
        super(integrationTestGenerator);
    }

    @Override
    public Integer priority() {
        return 1;
    }

    @Override
    public String name() {
        return "Create Entity";
    }

    @Override
    public String templatePath() {
        return "/integration_test/methods/CreateITMethod.tempjava";
    }

    @Override
    public boolean isAvailable(Class<?> resourceClass, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
        if (handlerMethod.getMethod().getParameterTypes().length == 1 && integrationTestGenerator.isEntity(handlerMethod.getMethod().getParameterTypes()[0])) {
            Class<?> entityClass = handlerMethod.getMethod().getParameterTypes()[0];
            Map<IntegrationTestGenerator.eUsageClassType, Pair<Class<?>, String>> usageClassTypes = integrationTestGenerator.getClassFieldStorage().get(entityClass);
            if (usageClassTypes.containsKey(IntegrationTestGenerator.eUsageClassType.Entity) && usageClassTypes.containsKey(IntegrationTestGenerator.eUsageClassType.Repository)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String createMethod(Class<?> resourceClass, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod, Set<Class<?>> importClasses, Map<ITemplateVariableEnum, CharSequence> templateValueMap, CreateProperties createProperties) throws Exception {
        Class<?> dtoClass = handlerMethod.getMethod().getParameterTypes()[0];
        Map<IntegrationTestGenerator.eUsageClassType, Pair<Class<?>, String>> usageClassTypes = integrationTestGenerator.getClassFieldStorage().get(dtoClass);
        Class<?> entityClass = usageClassTypes.get(IntegrationTestGenerator.eUsageClassType.Entity).getFirst();
        importClasses.add(entityClass);
        templateValueMap.put(eIntegrationTestMethodVariable.RequestType, requestMappingInfo.getMethodsCondition().getMethods().iterator().next().name().toLowerCase());
        templateValueMap.put(eIntegrationTestMethodVariable.RequestRoute, requestMappingInfo.getPatternsCondition().getPatterns().iterator().next());
        templateValueMap.put(eIntegrationTestMethodVariable.Entity, entityClass.getSimpleName());
        templateValueMap.put(eIntegrationTestMethodVariable.EntityVariable, usageClassTypes.get(IntegrationTestGenerator.eUsageClassType.Entity).getSecond());
        templateValueMap.put(eIntegrationTestMethodVariable.RepositoryVariable, usageClassTypes.get(IntegrationTestGenerator.eUsageClassType.Repository).getSecond());


        Object entityObject = entityClass.getDeclaredConstructor().newInstance();
        DummyDataReflectionUtil.fillParametersRandom(entityObject, entityClass, 0, integrationTestGenerator.getClassJpaRepositoryMap(), new HashMap<>());

        StringBuilder setsSB = new StringBuilder();
        StringBuilder assertsSB = new StringBuilder();

        for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(entityClass).getPropertyDescriptors()) {
            if (propertyDescriptor.getReadMethod() != null && propertyDescriptor.getWriteMethod() != null && !propertyDescriptor.getReadMethod().getName().equals("getClass")
                    && !propertyDescriptor.getReadMethod().getName().equals("getId") && propertyDescriptor.getReadMethod().invoke(entityObject) != null) {
                try {
                    Object value = propertyDescriptor.getReadMethod().invoke(entityObject);
                    String valueStr = FieldUtil.createGeneratorCode(propertyDescriptor.getPropertyType(), value.toString());
                    importClasses.add(propertyDescriptor.getPropertyType());
                    setsSB.append("        ").append(templateValueMap.get(eIntegrationTestMethodVariable.EntityVariable)).append(".")
                            .append(propertyDescriptor.getWriteMethod().getName()).append("(").append(valueStr).append(");\n");
                    assertsSB.append("        assertThat(test").append(templateValueMap.get(eIntegrationTestMethodVariable.Entity)).append(".").append(propertyDescriptor.getReadMethod().getName())
                            .append("()).isEqualTo(").append(valueStr).append(");\n");
                } catch (IllegalStateException e) {
                    log.warn(e.getMessage());
                }
            }
        }

        templateValueMap.put(eIntegrationTestMethodVariable.Sets, setsSB.toString());
        templateValueMap.put(eIntegrationTestMethodVariable.Asserts, assertsSB.toString());
        templateValueMap.put(eIntegrationTestMethodVariable.WarningMessage, "Method Successfully Created!");
        return IntegrationTestTemplateHelper.generateTemplate(templatePath(), (Map<ITemplateVariableEnum, CharSequence>) (Map<?, ?>) templateValueMap);
    }
}
