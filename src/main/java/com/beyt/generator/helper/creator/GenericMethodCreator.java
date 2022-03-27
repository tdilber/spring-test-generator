package com.beyt.generator.helper.creator;

import com.beyt.generator.domain.enumeration.ITemplateVariableEnum;
import com.beyt.generator.domain.enumeration.eIntegrationTestMethodVariable;
import com.beyt.generator.helper.IntegrationTestTemplateHelper;
import com.beyt.generator.manager.IntegrationTestGenerator;
import com.beyt.generator.util.DummyDataReflectionUtil;
import com.beyt.generator.util.GenericTypeResolverUtil;
import com.beyt.generator.util.field.FieldUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.Introspector;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tdilber at 11/17/2020
 */
@Slf4j
public class GenericMethodCreator extends BaseMethodCreator {
    public GenericMethodCreator(IntegrationTestGenerator integrationTestGenerator) {
        super(integrationTestGenerator);
    }

    @Override
    public Integer priority() {
        return 1;
    }

    @Override
    public String name() {
        return "Generic Get";
    }

    @Override
    public String templatePath() {
        return "/integration_test/methods/GenericITMethod.tempjava";
    }

    @Override
    public boolean isAvailable(Class<?> resourceClass, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
        return true;
    }

    @Override
    public String createMethod(Class<?> resourceClass, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod, Set<Class<?>> importClasses, Map<ITemplateVariableEnum, CharSequence> templateValueMap, CreateProperties createProperties) throws Exception {
        templateValueMap.put(eIntegrationTestMethodVariable.RequestType, requestMappingInfo.getMethodsCondition().getMethods().iterator().next().name().toLowerCase());
        String requestRoute = requestMappingInfo.getPatternsCondition().getPatterns().iterator().next();
        templateValueMap.put(eIntegrationTestMethodVariable.RequestRoute, requestRoute);

        StringBuilder setsSB = new StringBuilder();
        StringBuilder assertsSB = new StringBuilder();
        StringBuilder headersSB = new StringBuilder();
        StringBuilder requestBodySB = new StringBuilder();
        StringBuilder paramsSB = new StringBuilder();


        Class<?> returnType = handlerMethod.getMethod().getReturnType();

        IF_BREAK:
        if (!returnType.equals(Void.TYPE)) {
            GenericTypeResolverUtil.GenericsTypesTreeResult genericsTypesTreeResult = GenericTypeResolverUtil.resolveReturnTypeGenericsTree(handlerMethod.getMethod());

            if (ArrayUtils.isNotEmpty(createProperties.getIgnoreMethodReturnGeneric())) {
                if (Arrays.asList(createProperties.getIgnoreMethodReturnGeneric()).contains(genericsTypesTreeResult.getClazz())) {
                    if (CollectionUtils.isEmpty(genericsTypesTreeResult.getSubClasses())) {
                        break IF_BREAK;
                    } else {
                        genericsTypesTreeResult = genericsTypesTreeResult.getSubClasses().get(0);
                    }
                }
            }


            if (FieldUtil.isSupportedType(genericsTypesTreeResult.getClazz())) {
                importClasses.addAll(genericsTypesTreeResult.getAllClasses());
                assertsSB.append("        ").append(genericsTypesTreeResult.printResult()).append(" resultValue = TestUtil.getResultValue(mvcResult.getResponse().getContentAsString(), ").append(genericsTypesTreeResult.getClazz().getSimpleName()).append(".class);");
            } else if (Collection.class.isAssignableFrom(genericsTypesTreeResult.getClazz()) && CollectionUtils.isNotEmpty(genericsTypesTreeResult.getSubClasses())) {
                importClasses.addAll(genericsTypesTreeResult.getAllClasses());
                genericsTypesTreeResult.setClazz(List.class);
                Class<?> collectionClass = genericsTypesTreeResult.getSubClasses().get(0).getClazz();
                assertsSB.append("        ").append(genericsTypesTreeResult.printResult()).append(" resultValue = TestUtil.getResultListValue(mvcResult.getResponse().getContentAsString(), ").append(collectionClass.getSimpleName()).append("[].class);");
                putAssertsWithGetter(assertsSB, "resultValue", "size", "resultValue.size()");
                Object returnObject = collectionClass.getDeclaredConstructor().newInstance();
                DummyDataReflectionUtil.fillParametersRandom(returnObject, collectionClass, 0, integrationTestGenerator.getClassJpaRepositoryMap(), new HashMap<>());
                assertsSB.append("        for (int i = 0; i < resultValue.size(); i++) {\n");
                for (PropertyValues propertyDescriptor : getPropertyDescriptors(collectionClass, returnObject)) {
                    importClasses.add(propertyDescriptor.getClazz());
                    assertsSB.append("        ");
                    putAssertsWithGetter(assertsSB, "resultValue.get(i)", propertyDescriptor.getGetterMethod(), propertyDescriptor.getValueStr());
                }
                assertsSB.append("        }\n");
            }

//            else if(genericsTypesTreeResult.getClazz().isArray()) { // TODO maybe
//                importClasses.addAll(genericsTypesTreeResult.getAllClasses());
//                genericsTypesTreeResult.setClazz(List.class);
//                assertsSB.append("        ").append(genericsTypesTreeResult.printResult()).append(" resultValue = TestUtil.getResultListValue(mvcResult.getResponse().getContentAsString(), ").append(genericsTypesTreeResult.getClazz().getSimpleName()).append("[].class);");
//            }
        }


        for (Parameter parameter : handlerMethod.getMethod().getParameters()) {
            Class<?> parameterType = parameter.getType();
            if (parameterType.equals(HttpServletRequest.class) || parameterType.equals(HttpServletResponse.class)) {
                continue;
            }
            String variableName = parameter.getName();
            if (FieldUtil.isSupportedType(parameterType)) {
                String insideValue = FieldUtil.fillRandom(parameterType).toString();
                String value = FieldUtil.createGeneratorCode(parameterType, insideValue);
                setsSB.append("        ").append(parameterType.getSimpleName()).append(" ").append(variableName).append(" = ").append(value).append(";\n");
                ;
                assertsSB.append("        assertThat(").append(variableName).append(").isEqualTo(").append(value).append(");\n");

                if (Objects.nonNull(parameter.getAnnotation(PathVariable.class))) {
                    templateValueMap.put(eIntegrationTestMethodVariable.RequestRoute, requestRoute.replace("{" + variableName + "}", insideValue));
                }
            } else {
                Object parameterObject = parameterType.getDeclaredConstructor().newInstance();
                DummyDataReflectionUtil.fillParametersRandom(parameterObject, parameterType, 0, integrationTestGenerator.getClassJpaRepositoryMap(), new HashMap<>());
                setsSB.append("        ").append(parameterType.getSimpleName()).append(" ").append(variableName).append(" = new ").append(parameterType.getSimpleName()).append("();").append("\n");
                for (PropertyValues propertyDescriptor : getPropertyDescriptors(parameterType, parameterObject)) {
                    importClasses.add(propertyDescriptor.getClazz());
                    putSettersWithGetter(setsSB, variableName, propertyDescriptor.getSetterMethod(), propertyDescriptor.getValueStr());
                    putAssertsWithGetter(assertsSB, variableName, propertyDescriptor.getGetterMethod(), propertyDescriptor.getValueStr());
                }
            }
            if (Objects.nonNull(parameter.getAnnotation(RequestParam.class))) {
                paramsSB.append("                        .param(\"").append(variableName).append("\", ").append(variableName).append(".toString())\n");
            }
            if (Objects.nonNull(parameter.getAnnotation(RequestHeader.class))) {
                headersSB.append("                        .header(\"").append(variableName).append("\", ").append(variableName).append(")\n");
            }
            if (Objects.nonNull(parameter.getAnnotation(RequestBody.class))) {
                requestBodySB.append("                        .content(TestUtil.convertObjectToJsonBytes(").append(variableName).append("))");
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

    private void putSettersWithGetter(StringBuilder setsSB, String variableName, String methodName, String valueStr) {
        setsSB.append("        ").append(variableName).append(".")
                .append(methodName).append("(").append(valueStr).append(");\n");
    }

    private void putAssertsWithGetter(StringBuilder assertsSB, String variableName, String methodName, String valueStr) {
        assertsSB.append("        assertThat(").append(variableName).append(".").append(methodName)
                .append("()).isEqualTo(").append(valueStr).append(");\n");
    }

    private List<PropertyValues> getPropertyDescriptors(Class<?> parameterType, Object parameterObject) throws Exception {
        return Arrays.stream(Introspector.getBeanInfo(parameterType).getPropertyDescriptors()).filter(propertyDescriptor -> {
            try {
                return propertyDescriptor.getReadMethod() != null && propertyDescriptor.getWriteMethod() != null && !propertyDescriptor.getReadMethod().getName().equals("getClass")
                        && !propertyDescriptor.getReadMethod().getName().equals("getId") && propertyDescriptor.getReadMethod().invoke(parameterObject) != null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).map(p -> {
            Object value;
            String valueStr;
            try {
                value = p.getReadMethod().invoke(parameterObject);
                valueStr = FieldUtil.createGeneratorCode(p.getPropertyType(), value.toString());
            } catch (Exception e) {
                return null;
            }

            return new PropertyValues(p.getReadMethod().getName(), p.getWriteMethod().getName(), value, valueStr, p.getPropertyType());
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Data
    @AllArgsConstructor
    public static class PropertyValues {
        private String getterMethod;
        private String setterMethod;
        private Object value;
        private String valueStr;
        private Class<?> clazz;
    }
}
