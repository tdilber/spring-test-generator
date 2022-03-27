package com.beyt.generator.helper;

import com.beyt.generator.domain.enumeration.ITemplateVariableEnum;
import com.beyt.generator.helper.creator.*;
import com.beyt.generator.manager.IntegrationTestGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.util.*;

/**
 * Created by tdilber at 11/17/2020
 */
@Slf4j
@Component
@Profile("dev")
public class IntegrationTestMethodGenerator {
    private List<IMethodCreator> methodCreators = new ArrayList<>();
    private final IntegrationTestGenerator integrationTestGenerator;

    public IntegrationTestMethodGenerator(IntegrationTestGenerator integrationTestGenerator) {
        this.integrationTestGenerator = integrationTestGenerator;
        methodCreators.add(new CreateEntityDTOMethodCreator(integrationTestGenerator));
        methodCreators.add(new CreateEntityMethodCreator(integrationTestGenerator));
        methodCreators.add(new UpdateEntityDTOMethodCreator(integrationTestGenerator));
        methodCreators.add(new DeleteEntityDTOMethodCreator(integrationTestGenerator));
        methodCreators.add(new GetEntityDTOMethodCreator(integrationTestGenerator));
        methodCreators.add(new GetAllEntityDTOMethodCreator(integrationTestGenerator));
        methodCreators.add(new GenericMethodCreator(integrationTestGenerator));
        methodCreators.sort(Comparator.comparing(IMethodCreator::priority));
    }

    public @Nullable
    String methodCreate(Class<?> resourceClass, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod, Set<Class<?>> importClasses, Map<ITemplateVariableEnum, CharSequence> templateValueMap, com.beyt.generator.annotation.IntegrationTestGenerator integrationTestMethodGenerator) throws Exception {
        for (IMethodCreator methodCreator : methodCreators) {
            if (methodCreator.isAvailable(resourceClass, requestMappingInfo, handlerMethod)) {
                try {
                    return methodCreator.createMethod(resourceClass, requestMappingInfo, handlerMethod, importClasses, templateValueMap, new IMethodCreator.CreateProperties(integrationTestMethodGenerator.ignoreMethodReturnGeneric()));
                } catch (Exception e) {
                    throw new Exception("MethodName: " + methodCreator.name() + " failed! " + e.getMessage(), e);
                }
            }
        }
        return null;
    }
}
