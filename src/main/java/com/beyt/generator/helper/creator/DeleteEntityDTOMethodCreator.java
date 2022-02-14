package com.beyt.generator.helper.creator;

import com.beyt.generator.domain.enumeration.ITemplateVariableEnum;
import com.beyt.generator.domain.enumeration.eIntegrationTestMethodVariable;
import com.beyt.generator.helper.IntegrationTestTemplateHelper;
import com.beyt.generator.manager.IntegrationTestGenerator;
import com.beyt.generator.util.DummyDataReflectionUtil;
import com.beyt.generator.util.field.FieldUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class DeleteEntityDTOMethodCreator extends BaseMethodCreator {

    public DeleteEntityDTOMethodCreator(IntegrationTestGenerator integrationTestGenerator) {
        super(integrationTestGenerator);
    }

    @Override
    public Integer priority() {
        return 1;
    }

    @Override
    public String name() {
        return "Delete Entity(DTO)";
    }

    @Override
    public String templatePath() {
        return "/integration_test/methods/DeleteITMethod.tempjava";
    }

    @Override
    public boolean isAvailable(Class<?> resourceClass, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
        if (handlerMethod.getMethod().getName().startsWith("delete") && handlerMethod.getMethod().getParameterTypes().length == 1 && (Long.class.isAssignableFrom(handlerMethod.getMethod().getParameterTypes()[0]))) {
            if (integrationTestGenerator.getResourceFieldStorage().get(resourceClass).containsKey(IntegrationTestGenerator.eUsageClassType.Repository)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String createMethod(Class<?> resourceClass, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod, Set<Class<?>> importClasses) throws Exception {
        Map<eIntegrationTestMethodVariable, String> templateValueMap = new HashMap<>();
        templateValueMap.put(eIntegrationTestMethodVariable.MethodName, handlerMethod.getMethod().getName());
        Class<?> entityClass = integrationTestGenerator.getResourceFieldStorage().get(resourceClass).get(IntegrationTestGenerator.eUsageClassType.Entity).getFirst();
        importClasses.add(entityClass);
        templateValueMap.put(eIntegrationTestMethodVariable.RequestType, requestMappingInfo.getMethodsCondition().getMethods().iterator().next().name().toLowerCase());
        templateValueMap.put(eIntegrationTestMethodVariable.RequestRoute, requestMappingInfo.getPatternsCondition().getPatterns().iterator().next());
        templateValueMap.put(eIntegrationTestMethodVariable.Entity, entityClass.getSimpleName());
        templateValueMap.put(eIntegrationTestMethodVariable.EntityVariable, integrationTestGenerator.getResourceFieldStorage().get(resourceClass).get(IntegrationTestGenerator.eUsageClassType.Entity).getSecond());
        templateValueMap.put(eIntegrationTestMethodVariable.RepositoryVariable, integrationTestGenerator.getResourceFieldStorage().get(resourceClass).get(IntegrationTestGenerator.eUsageClassType.Repository).getSecond());


        Object entityObject = entityClass.getDeclaredConstructor().newInstance();
        DummyDataReflectionUtil.fillParametersRandom(entityObject, entityClass, 0, integrationTestGenerator.getClassJpaRepositoryMap(), new HashMap<>());

        StringBuilder setsSB = new StringBuilder();

        for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(entityClass).getPropertyDescriptors()) {
            if (propertyDescriptor.getReadMethod() != null && propertyDescriptor.getWriteMethod() != null && !propertyDescriptor.getReadMethod().getName().equals("getClass")
                    && !propertyDescriptor.getReadMethod().getName().equals("getId") && propertyDescriptor.getReadMethod().invoke(entityObject) != null) {
                try {
                    Object value = propertyDescriptor.getReadMethod().invoke(entityObject);
                    String valueStr = FieldUtil.createGeneratorCode(propertyDescriptor.getPropertyType(), value.toString());
                    importClasses.add(propertyDescriptor.getPropertyType());
                    setsSB.append("        ").append(templateValueMap.get(eIntegrationTestMethodVariable.EntityVariable)).append(".")
                            .append(propertyDescriptor.getWriteMethod().getName()).append("(").append(valueStr).append(");\n");
                } catch (IllegalStateException e) {
                    log.warn(e.getMessage());
                }
            }
        }

        templateValueMap.put(eIntegrationTestMethodVariable.Sets, setsSB.toString());
        templateValueMap.put(eIntegrationTestMethodVariable.WarningMessage, "Method Successfully Created!");
        return IntegrationTestTemplateHelper.generateTemplate(templatePath(), (Map<ITemplateVariableEnum, CharSequence>) (Map<?, ?>) templateValueMap);
    }
}
