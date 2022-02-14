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

@Slf4j
public class UpdateEntityDTOMethodCreator extends BaseMethodCreator {
    public UpdateEntityDTOMethodCreator(IntegrationTestGenerator integrationTestGenerator) {
        super(integrationTestGenerator);
    }

    @Override
    public Integer priority() {
        return 5;
    }

    @Override
    public String name() {
        return "Update Entity(DTO)";
    }

    @Override
    public String templatePath() {
        return "/integration_test/methods/UpdateITMethod.tempjava";
    }

    @Override
    public boolean isAvailable(Class<?> resourceClass, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
        if (handlerMethod.getMethod().getName().startsWith("update") && handlerMethod.getMethod().getParameterTypes().length == 1 && integrationTestGenerator.isEntityDTO(handlerMethod.getMethod().getParameterTypes()[0])) {
            Class<?> dtoClass = handlerMethod.getMethod().getParameterTypes()[0];
            Map<IntegrationTestGenerator.eUsageClassType, Pair<Class<?>, String>> usageClassTypes = integrationTestGenerator.getClassFieldStorage().get(dtoClass);
            if (usageClassTypes.containsKey(IntegrationTestGenerator.eUsageClassType.Entity) && usageClassTypes.containsKey(IntegrationTestGenerator.eUsageClassType.EntityDto) && usageClassTypes.containsKey(IntegrationTestGenerator.eUsageClassType.ObjectMapper) && usageClassTypes.containsKey(IntegrationTestGenerator.eUsageClassType.Repository)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String createMethod(Class<?> resourceClass, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod, Set<Class<?>> importClasses) throws Exception {
        Map<eIntegrationTestMethodVariable, String> templateValueMap = new HashMap<>();
        Class<?> dtoClass = handlerMethod.getMethod().getParameterTypes()[0];
        templateValueMap.put(eIntegrationTestMethodVariable.MethodName, handlerMethod.getMethod().getName());
        Map<IntegrationTestGenerator.eUsageClassType, Pair<Class<?>, String>> usageClassTypes = integrationTestGenerator.getClassFieldStorage().get(dtoClass);
        Class<?> entityClass = usageClassTypes.get(IntegrationTestGenerator.eUsageClassType.Entity).getFirst();
        importClasses.add(entityClass);
        importClasses.add(usageClassTypes.get(IntegrationTestGenerator.eUsageClassType.EntityDto).getFirst());
        templateValueMap.put(eIntegrationTestMethodVariable.RequestType, requestMappingInfo.getMethodsCondition().getMethods().iterator().next().name().toLowerCase());
        templateValueMap.put(eIntegrationTestMethodVariable.RequestRoute, requestMappingInfo.getPatternsCondition().getPatterns().iterator().next());
        templateValueMap.put(eIntegrationTestMethodVariable.Entity, entityClass.getSimpleName());
        templateValueMap.put(eIntegrationTestMethodVariable.EntityVariable, usageClassTypes.get(IntegrationTestGenerator.eUsageClassType.Entity).getSecond());
        templateValueMap.put(eIntegrationTestMethodVariable.RepositoryVariable, usageClassTypes.get(IntegrationTestGenerator.eUsageClassType.Repository).getSecond());
        templateValueMap.put(eIntegrationTestMethodVariable.DTO, usageClassTypes.get(IntegrationTestGenerator.eUsageClassType.EntityDto).getFirst().getSimpleName());
        templateValueMap.put(eIntegrationTestMethodVariable.DtoVariable, usageClassTypes.get(IntegrationTestGenerator.eUsageClassType.EntityDto).getSecond());
        templateValueMap.put(eIntegrationTestMethodVariable.MapperVariable, usageClassTypes.get(IntegrationTestGenerator.eUsageClassType.ObjectMapper).getSecond());


        Object entityObject = entityClass.getDeclaredConstructor().newInstance();
        Object entityObject2 = entityClass.getDeclaredConstructor().newInstance();
        DummyDataReflectionUtil.fillParametersRandom(entityObject, entityClass, 0, integrationTestGenerator.getClassJpaRepositoryMap(), new HashMap<>());
        DummyDataReflectionUtil.fillParametersRandom(entityObject2, entityClass, 1, integrationTestGenerator.getClassJpaRepositoryMap(), new HashMap<>());

        StringBuilder setsSB = new StringBuilder();
        StringBuilder upsSB = new StringBuilder();
        StringBuilder assertsSB = new StringBuilder();

        for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(entityClass).getPropertyDescriptors()) {
            if (propertyDescriptor.getReadMethod() != null && propertyDescriptor.getWriteMethod() != null && !propertyDescriptor.getReadMethod().getName().equals("getClass")
                    && !propertyDescriptor.getReadMethod().getName().equals("getId") && propertyDescriptor.getReadMethod().invoke(entityObject) != null) {
                try {
                    Object value = propertyDescriptor.getReadMethod().invoke(entityObject);
                    Object value2 = propertyDescriptor.getReadMethod().invoke(entityObject2);
                    String valueStr = FieldUtil.createGeneratorCode(propertyDescriptor.getPropertyType(), value.toString());
                    String valueStr2 = FieldUtil.createGeneratorCode(propertyDescriptor.getPropertyType(), value2.toString());
                    importClasses.add(propertyDescriptor.getPropertyType());
                    setsSB.append("        ").append(templateValueMap.get(eIntegrationTestMethodVariable.EntityVariable)).append(".")
                            .append(propertyDescriptor.getWriteMethod().getName()).append("(").append(valueStr).append(");\n");
                    upsSB.append("        ").append(templateValueMap.get(eIntegrationTestMethodVariable.EntityVariable)).append(".")
                            .append(propertyDescriptor.getWriteMethod().getName()).append("(").append(valueStr2).append(");\n");
                    assertsSB.append("        assertThat(test").append(templateValueMap.get(eIntegrationTestMethodVariable.Entity)).append(".").append(propertyDescriptor.getReadMethod().getName())
                            .append("()).isEqualTo(").append(valueStr2).append(");\n");

                } catch (IllegalStateException e) {
                    log.warn(e.getMessage());
                }
            }
        }

        templateValueMap.put(eIntegrationTestMethodVariable.Sets, setsSB.toString());
        templateValueMap.put(eIntegrationTestMethodVariable.Asserts, assertsSB.toString());
        templateValueMap.put(eIntegrationTestMethodVariable.Updates, upsSB.toString());
        templateValueMap.put(eIntegrationTestMethodVariable.WarningMessage, "Method Successfully Created!");
        return IntegrationTestTemplateHelper.generateTemplate(templatePath(), (Map<ITemplateVariableEnum, CharSequence>) (Map<?, ?>) templateValueMap);
    }
}
