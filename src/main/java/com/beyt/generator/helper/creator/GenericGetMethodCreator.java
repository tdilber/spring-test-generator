package com.beyt.generator.helper.creator;

import com.beyt.generator.domain.enumeration.ITemplateVariableEnum;
import com.beyt.generator.domain.enumeration.eIntegrationTestMethodVariable;
import com.beyt.generator.helper.IntegrationTestTemplateHelper;
import com.beyt.generator.manager.IntegrationTestGenerator;
import com.beyt.generator.util.DummyDataReflectionUtil;
import com.beyt.generator.util.field.FieldUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by tdilber at 11/17/2020
 */
@Slf4j
public class GenericGetMethodCreator extends BaseMethodCreator {
    public GenericGetMethodCreator(IntegrationTestGenerator integrationTestGenerator) {
        super(integrationTestGenerator);
    }

    @Override
    public Integer priority() {
        return 10;
    }

    @Override
    public String name() {
        return "Generic Get";
    }

    @Override
    public String templatePath() {
        return "/integration_test/methods/GenericGetITMethod.tempjava";
    }

    @Override
    public boolean isAvailable(Class<?> resourceClass, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
        return true;
    }

    @Override
    public String createMethod(Class<?> resourceClass, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod, Set<Class<?>> importClasses) throws Exception {
        Map<eIntegrationTestMethodVariable, String> templateValueMap = new HashMap<>();
        templateValueMap.put(eIntegrationTestMethodVariable.RequestType, requestMappingInfo.getMethodsCondition().getMethods().iterator().next().name().toLowerCase());
        templateValueMap.put(eIntegrationTestMethodVariable.RequestRoute, requestMappingInfo.getPatternsCondition().getPatterns().iterator().next());


        templateValueMap.put(eIntegrationTestMethodVariable.MethodName, handlerMethod.getMethod().getName());

        StringBuilder setsSB = new StringBuilder();
        StringBuilder assertsSB = new StringBuilder();
        StringBuilder headersSB = new StringBuilder();
        StringBuilder requestBodySB = new StringBuilder();
        StringBuilder paramsSB = new StringBuilder();

        for (Parameter  parameter : handlerMethod.getMethod().getParameters()) {
            Class<? >parameterType = parameter.getType();
            if (parameterType.equals(HttpServletRequest.class) || parameterType.equals(HttpServletResponse.class)) {
                continue;
            }
            String variableName = parameter.getName();
            if (FieldUtil.isSupportedType(parameterType)) {
                String insideValue = FieldUtil.fillRandom(parameterType).toString();
                String value = FieldUtil.createGeneratorCode(parameterType, insideValue);
                setsSB.append("        ").append(parameterType.getSimpleName()).append(" ").append(variableName).append(" = ").append(value).append(";\n");;
                assertsSB.append("        assertThat(").append(variableName).append(").isEqualTo(").append(value).append(");\n");

                if (Objects.nonNull(parameter.getAnnotation(PathVariable.class))) {
                    String path = templateValueMap.get(eIntegrationTestMethodVariable.RequestRoute);
                    templateValueMap.put(eIntegrationTestMethodVariable.RequestRoute, path.replace("{" + variableName + "}", insideValue));
                }
            }else {
                Object parameterObject = parameterType.getDeclaredConstructor().newInstance();
                DummyDataReflectionUtil.fillParametersRandom(parameterObject, parameterType, 0, integrationTestGenerator.getClassJpaRepositoryMap(), new HashMap<>());
                setsSB.append("        ").append(parameterType.getSimpleName()).append(" ").append(variableName).append(" = new ").append(parameterType.getSimpleName()).append("();").append("\n");
                for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(parameterType).getPropertyDescriptors()) {
                    if (propertyDescriptor.getReadMethod() != null && propertyDescriptor.getWriteMethod() != null && !propertyDescriptor.getReadMethod().getName().equals("getClass")
                            && !propertyDescriptor.getReadMethod().getName().equals("getId") && propertyDescriptor.getReadMethod().invoke(parameterObject) != null) {
                        try {
                            Object value = propertyDescriptor.getReadMethod().invoke(parameterObject);
                            String valueStr = FieldUtil.createGeneratorCode(propertyDescriptor.getPropertyType(), value.toString());
                            importClasses.add(propertyDescriptor.getPropertyType());
                            setsSB.append("        ").append(variableName).append(".")
                                    .append(propertyDescriptor.getWriteMethod().getName()).append("(").append(valueStr).append(");\n");
                            assertsSB.append("        assertThat(").append(variableName).append(".").append(propertyDescriptor.getReadMethod().getName())
                                    .append("()).isEqualTo(").append(valueStr).append(");\n");
                        } catch (IllegalStateException e) {
                            log.warn(e.getMessage());
                        }
                    }
                }

            }
            if (Objects.nonNull(parameter.getAnnotation(RequestParam.class))) {
                paramsSB.append("                        .param(\"").append(variableName).append("\", ").append(variableName).append(".toString())\n");
            }
            if (Objects.nonNull(parameter.getAnnotation(RequestHeader.class))) {
                headersSB.append("                        .header(\"").append(variableName).append("\", ").append(variableName).append(")\n");
            }
            if (Objects.nonNull(parameter.getAnnotation(RequestBody.class))) {
                requestBodySB.append(".content(TestUtil.convertObjectToJsonBytes(").append(variableName).append("))");
            }
            importClasses.add(parameterType);
        }


        templateValueMap.put(eIntegrationTestMethodVariable.Headers, headersSB.toString());
        templateValueMap.put(eIntegrationTestMethodVariable.RequestBody, requestBodySB.toString());
        templateValueMap.put(eIntegrationTestMethodVariable.Params, paramsSB.toString());
        templateValueMap.put(eIntegrationTestMethodVariable.Sets, setsSB.toString());
        templateValueMap.put(eIntegrationTestMethodVariable.Asserts, assertsSB.toString());
        templateValueMap.put(eIntegrationTestMethodVariable.WarningMessage, "Method Successfully Created!");
        return IntegrationTestTemplateHelper.generateTemplate(templatePath(), (Map<ITemplateVariableEnum, CharSequence>) (Map<?, ?>) templateValueMap);
    }
}
