package com.beyt.generator.helper;

import com.beyt.generator.helper.creator.*;
import com.beyt.generator.manager.IntegrationTestGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    }

    public @Nullable
    String methodCreate(Class<?> resourceClass, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod, Set<Class<?>> importClasses) throws Exception {
        for (IMethodCreator methodCreator : methodCreators) {
            if (methodCreator.isAvailable(resourceClass, requestMappingInfo, handlerMethod)) {
                try {
                    return methodCreator.createMethod(resourceClass, requestMappingInfo, handlerMethod, importClasses);
                } catch (Exception e) {
                    throw new Exception("MethodName: " + methodCreator.name() + " failed! " + e.getMessage(), e);
                }
            }
        }
        return null;
    }
}