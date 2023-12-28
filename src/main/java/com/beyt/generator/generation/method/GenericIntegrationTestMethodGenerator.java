package com.beyt.generator.generation.method;

import com.beyt.generator.annotation.IntegrationTestGenerator;
import com.beyt.generator.domain.enumeration.ITemplateVariableEnum;
import com.beyt.generator.domain.enumeration.eIntegrationTestMethodVariable;
import com.beyt.generator.generation.parameter.MethodGenerationParameter;
import com.beyt.generator.generation.parameter.RestMethodRecordGenerationParameter;
import com.beyt.generator.helper.IntegrationTestCodePrepareHelper;
import com.beyt.generator.helper.IntegrationTestTemplateHelper;
import com.beyt.generator.util.GenericTypeResolverUtil;
import com.beyt.generator.util.field.FieldUtil;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Parameter;
import java.util.*;

public class GenericIntegrationTestMethodGenerator implements IntegrationTestMethodGenerator {
    private final IntegrationTestGenerator integrationTestGenerator;

    public GenericIntegrationTestMethodGenerator(IntegrationTestGenerator integrationTestGenerator) {
        this.integrationTestGenerator = integrationTestGenerator;
    }

    public String templatePath() {
        return "/integration_test/methods/GenericITMethod.tempjava";
    }

    @Override
    @SneakyThrows
    public Result generate(MethodGenerationParameter param) {
        if (!(param instanceof RestMethodRecordGenerationParameter generationParameter)) {
            throw new IllegalStateException("parameter must be RestMethodRecordGenerationParameter");
        }
        HandlerMethod handlerMethod = generationParameter.getHandlerMethod();
        RequestMappingInfo requestMappingInfo = generationParameter.getRequestMappingInfo();

        Map<eIntegrationTestMethodVariable, String> templateValueMap = generationParameter.getGenerationVariableMap();
        Set<Class<?>> importClasses = new HashSet<>();
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

            if (ArrayUtils.isNotEmpty(integrationTestGenerator.ignoreMethodReturnGeneric())) {
                if (Arrays.asList(integrationTestGenerator.ignoreMethodReturnGeneric()).contains(genericsTypesTreeResult.getClazz())) {
                    if (CollectionUtils.isEmpty(genericsTypesTreeResult.getSubClasses())) {
                        break IF_BREAK;
                    } else {
                        genericsTypesTreeResult = genericsTypesTreeResult.getSubClasses().get(0);
                    }
                }
            }

            if (Collection.class.isAssignableFrom(genericsTypesTreeResult.getClazz()) && CollectionUtils.isNotEmpty(genericsTypesTreeResult.getSubClasses())) {
                importClasses.addAll(genericsTypesTreeResult.getAllClasses());
                genericsTypesTreeResult.setClazz(List.class);
                Class<?> collectionClass = genericsTypesTreeResult.getSubClasses().get(0).getClazz();
                assertsSB.append("        ").append(genericsTypesTreeResult.printResult()).append(" resultValue = TestUtil.getResultListValue(mvcResult.getResponse().getContentAsString(), ").append(collectionClass.getSimpleName()).append("[].class);");
                Collection<?> returnObject = (Collection<?>) generationParameter.getReturnValue();
                List<?> list = IterableUtils.toList(returnObject);
                IntegrationTestCodePrepareHelper.assertsWithGetter(assertsSB, "resultValue", "size", list.size() + "");
                for (int i = 0; i < list.size(); i++) {
                    IntegrationTestCodePrepareHelper.assertsForObject("resultValue.get(" + i + ")", collectionClass, list.get(i), importClasses, assertsSB);
                }
            } else {
                importClasses.addAll(genericsTypesTreeResult.getAllClasses());
                assertsSB.append("        ").append(genericsTypesTreeResult.printResult()).append(" resultValue = TestUtil.getResultValue(mvcResult.getResponse().getContentAsString(), ").append(genericsTypesTreeResult.getClazz().getSimpleName()).append(".class);\n");

                if (FieldUtil.isSupportedType(genericsTypesTreeResult.getClazz())) {
                    String insideValue = generationParameter.getReturnValue().toString();
                    String value = FieldUtil.createGeneratorCode(genericsTypesTreeResult.getClazz(), insideValue);
                    IntegrationTestCodePrepareHelper.asserts(assertsSB, "resultValue", value);
                } else {
                    Object returnObject = generationParameter.getReturnValue();
                    IntegrationTestCodePrepareHelper.assertsForObject("resultValue", genericsTypesTreeResult.getClazz(), returnObject, importClasses, assertsSB);
                }
            } // TODO maybe genericsTypesTreeResult.getClazz().isArray()
        }


        for (int i = 0; i < handlerMethod.getMethod().getParameters().length; i++) {
            Parameter parameter = handlerMethod.getMethod().getParameters()[i];

            Class<?> parameterType = parameter.getType();
            if (ArrayUtils.contains(integrationTestGenerator.ignoreMethodArgTypes(), parameterType)) {
                continue;
            }
            String variableName = parameter.getName();
            if (FieldUtil.isSupportedType(parameterType)) {
                String insideValue = Objects.requireNonNullElse(generationParameter.getParameterArgs()[i], "null").toString();
                String value = FieldUtil.createGeneratorCode(parameterType, insideValue);
                IntegrationTestCodePrepareHelper.setters(setsSB, variableName, parameterType, value);
//                IntegrationTestCodePrepareHelper.asserts(assertsSB, variableName, value);
                if (Objects.nonNull(parameter.getAnnotation(PathVariable.class))) {
                    templateValueMap.put(eIntegrationTestMethodVariable.RequestRoute, requestRoute.replace("{" + variableName + "}", insideValue));
                }
            } else {
                Object parameterObject = generationParameter.getParameterArgs()[i];
                IntegrationTestCodePrepareHelper.settersForObject(variableName, parameterType, parameterObject, importClasses, setsSB);
//                IntegrationTestCodePrepareHelper.assertsForObject(variableName, parameterType, parameterObject, importClasses, assertsSB);
            }
            if (Objects.nonNull(parameter.getAnnotation(RequestParam.class))) {
                IntegrationTestCodePrepareHelper.requestParam(paramsSB, variableName);
            }
            if (Objects.nonNull(parameter.getAnnotation(RequestHeader.class))) {
                IntegrationTestCodePrepareHelper.headerParam(headersSB, variableName);
            }
            if (Objects.nonNull(parameter.getAnnotation(RequestBody.class))) {
                IntegrationTestCodePrepareHelper.requestBody(requestBodySB, variableName);
            }
            importClasses.add(parameterType);
        }

        if (MapUtils.isNotEmpty(generationParameter.getHeaderArgs())) {
            generationParameter.getHeaderArgs().forEach((key, value) -> {
                IntegrationTestCodePrepareHelper.headerParam(headersSB, key, value);
            });
        }


        templateValueMap.put(eIntegrationTestMethodVariable.Headers, headersSB.toString());
        templateValueMap.put(eIntegrationTestMethodVariable.RequestBody, requestBodySB.toString());
        templateValueMap.put(eIntegrationTestMethodVariable.Params, paramsSB.toString());
        templateValueMap.put(eIntegrationTestMethodVariable.Sets, setsSB.toString());
        templateValueMap.put(eIntegrationTestMethodVariable.Asserts, assertsSB.toString());
        templateValueMap.put(eIntegrationTestMethodVariable.WarningMessage, "Method Successfully Created!");
        return new Result(IntegrationTestTemplateHelper.generateTemplate(templatePath(), (Map<ITemplateVariableEnum, CharSequence>) (Map<?, ?>) templateValueMap), importClasses, templateValueMap);
    }
}
