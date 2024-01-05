package com.beyt.generator.generation;

import com.beyt.generator.annotation.IntegrationTestGenerator;
import com.beyt.generator.generation.method.value.MethodReturnIgnoreGenericConverter;
import com.beyt.generator.generation.parameter.RestMethodRecordGenerationParameter;
import com.beyt.generator.generation.test.IntegrationGenerator;
import com.beyt.generator.generation.test.ManualTestRecorderIntegrationGenerator;
import com.beyt.generator.generation.test.RandomValuesIntegrationGenerator;
import com.beyt.generator.helper.IntegrationTestTemplateHelper;
import com.beyt.generator.mapper.EntityMapper;
import com.beyt.generator.util.ApplicationContextUtil;
import com.beyt.generator.util.HashUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@Profile("integration-test-generator")
public class IntegrationTestGenerateManager implements ApplicationRunner {
    private final ApplicationContext applicationContext;
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Lazy
    @Autowired
    public void setRequestMappingHandlerMapping(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    private final EntityManager entityManager;
    private final List<EntityMapper<?, ?>> entityMapperList;
    private final java.util.List<JpaRepository<?, ?>> jpaRepositoryList;
    private final Map<Class<?>, MethodReturnIgnoreGenericConverter<?, ?>> ignoreMethodReturnGenericConverterMap;
    private IntegrationTestGenerator annotation = null;
    Map<Method, Pair<RequestMappingInfo, HandlerMethod>> methodPairMap = null;
    Set<String> alreadyGeneratedMethods = new HashSet<>();
    private IntegrationGenerator integrationGenerator;

    public IntegrationTestGenerateManager(ApplicationContext applicationContext, EntityManager entityManager, List<EntityMapper<?, ?>> entityMapperList, List<JpaRepository<?, ?>> jpaRepositoryList, List<MethodReturnIgnoreGenericConverter<?, ?>> ignoreMethodReturnGenericConverterList) {
        this.applicationContext = applicationContext;
        this.entityManager = entityManager;
        this.entityMapperList = entityMapperList;
        this.jpaRepositoryList = jpaRepositoryList;
        this.ignoreMethodReturnGenericConverterMap = ignoreMethodReturnGenericConverterList.stream().map(x -> Pair.of(x.getGenericType(), x)).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, (x, y) -> y));
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethodMap = requestMappingHandlerMapping.getHandlerMethods();
        methodPairMap = requestMappingInfoHandlerMethodMap.entrySet().stream().collect(Collectors.toMap(e -> e.getValue().getMethod(), e -> Pair.of(e.getKey(), e.getValue())));
        log.info("LiveTestGenerateInterceptor initialized!");

        annotation = ApplicationContextUtil.getFirstAnnotation(applicationContext, IntegrationTestGenerator.class);
        IntegrationTestTemplateHelper.checkFolderIsEmptyOrDeleteAll(annotation.outputPath(), annotation.deleteGenerationDirectory());
        IntegrationGenerator.Type type = annotation.generationType();
        switch (type) {
            case LIVE_TEST_RECORDER -> integrationGenerator = new ManualTestRecorderIntegrationGenerator(annotation);
            case TEST_WITH_RANDOM_DATA_GENERATOR -> integrationGenerator = new RandomValuesIntegrationGenerator();
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        }
        integrationGenerator.start();
        log.info("End Of Integration Test Generate Process");
    }

    public void appendLiveTest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Method method, final Object returnValue, Object[] parameterArgs) {
        if (integrationGenerator.type() != IntegrationGenerator.Type.LIVE_TEST_RECORDER || Objects.isNull(methodPairMap)) {
            return;
        }

        // extract headers
        Map<String, String> headerArgs = extractHeaders(httpServletRequest);

        //check if already generated
        String hash = HashUtil.getHash(method, parameterArgs, returnValue);
        if (alreadyGeneratedMethods.contains(hash)) return;
        else alreadyGeneratedMethods.add(hash);

        var pair = methodPairMap.get(method);
        Object finalReturnValue = convertReturnObjectIfIgnoreGenericMethod(returnValue);
        integrationGenerator.append(RestMethodRecordGenerationParameter.of(httpServletRequest, httpServletResponse, method, pair.getFirst(), pair.getSecond(), finalReturnValue, parameterArgs, headerArgs));
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headerArgs = new HashMap<>();
        if (Objects.nonNull(annotation.recordedRequestHeaders())) {
            Stream.of(annotation.recordedRequestHeaders()).forEach(x -> {
                String header = request.getHeader(x);
                if (Objects.nonNull(header)) {
                    headerArgs.put(x, header);
                }
            });
        }
        return headerArgs;
    }

    @SuppressWarnings("unchecked")
    private <T> Object convertReturnObjectIfIgnoreGenericMethod(T returnValue) {
        return Stream.of(annotation.ignoreMethodReturnGeneric()).filter(x -> x.isAssignableFrom(returnValue.getClass())).findFirst()
                .map(x -> {
                    MethodReturnIgnoreGenericConverter<T, ?> converter = (MethodReturnIgnoreGenericConverter<T, ?>) ignoreMethodReturnGenericConverterMap.get(x);
                    if (Objects.isNull(converter)) {
                        throw new IllegalArgumentException("MethodReturnIgnoreGenericConverter not found for " + x.getName());
                    }
                    return (Object) converter.convert(returnValue);
                }).orElse(returnValue);
    }

    @PreDestroy
    public void destroy() {
        if (integrationGenerator.type() != IntegrationGenerator.Type.LIVE_TEST_RECORDER || Objects.isNull(methodPairMap)) {
            return;
        }
        integrationGenerator.generate();
    }

    public record ITGContext(String method, Object returnValue, Object[] parameterArgs) {
    }
}
