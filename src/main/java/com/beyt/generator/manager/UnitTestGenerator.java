package com.beyt.generator.manager;

import com.beyt.generator.domain.enumeration.ITemplateVariableEnum;
import com.beyt.generator.domain.enumeration.eIntegrationTestMethodVariable;
import com.beyt.generator.domain.enumeration.eIntegrationTestVariable;
import com.beyt.generator.helper.IntegrationTestMethodGenerator;
import com.beyt.generator.helper.IntegrationTestTemplateHelper;
import com.beyt.generator.mapper.EntityMapper;
import com.beyt.generator.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.persistence.EntityManager;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by tdilber at 11/6/2020
 */
@Slf4j
@Service
@Profile("dev")
public class UnitTestGenerator implements ApplicationRunner {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private final ApplicationContext applicationContext;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final Map<eIntegrationTestVariable, StringBuilder> variableMap = new HashMap<>();
    private final List<ReflectionUtil.TwoGenericTypeResult<EntityMapper<?, ?>>> entityMapperGenericTypeResults;
    private final List<ReflectionUtil.TwoGenericTypeResult<JpaRepository<?, ?>>> classJpaRepositoryMap;
    private final Map<Class<?>, Map<eUsageClassType, Pair<Class<?>, String>>> classFieldStorage = new HashMap<>();
    private final Map<Class<?>, Map<eUsageClassType, Pair<Class<?>, String>>> resourceFieldStorage = new HashMap<>();
    private final EntityManager entityManager;
    private IntegrationTestMethodGenerator integrationTestMethodGenerator;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethodMap = requestMappingHandlerMapping.getHandlerMethods();
        com.beyt.generator.annotation.IntegrationTestGenerator integrationTestGenerator = ApplicationContextUtil.getFirstAnnotation(applicationContext, com.beyt.generator.annotation.IntegrationTestGenerator.class);
        IntegrationTestTemplateHelper.checkFolderIsEmptyOrDeleteAll(integrationTestGenerator.outputPath(), integrationTestGenerator.deleteGenerationDirectory());
        generateTestUtilClass(integrationTestGenerator);
        generateIntegrationTestClasses(requestMappingInfoHandlerMethodMap, integrationTestGenerator);
        log.info("End Of Integration Test Generate Process");
    }

    public enum eUsageClassType {
        Repository,
        EntityDto,
        Entity,
        ObjectMapper
    }

    public UnitTestGenerator(ApplicationContext applicationContext, RequestMappingHandlerMapping requestMappingHandlerMapping, List<EntityMapper<?, ?>> entityMapperList, List<JpaRepository<?, ?>> jpaRepositoryList, EntityManager entityManager) {
        this.applicationContext = applicationContext;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;

        entityMapperGenericTypeResults = ReflectionUtil.genericListToType2ObjectMap(entityMapperList, EntityMapper.class);
        classJpaRepositoryMap = ReflectionUtil.genericListToType2ObjectMap(jpaRepositoryList, JpaRepository.class);
        this.entityManager = entityManager;

        for (eIntegrationTestVariable integrationTestVariable : eIntegrationTestVariable.values()) {
            variableMap.put(integrationTestVariable, new StringBuilder());
        }
    }

    private void generateIntegrationTestClasses(Map<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethodMap, com.beyt.generator.annotation.IntegrationTestGenerator integrationTestGenerator) {
        Set<Class<?>> resourceClasses = new HashSet<>();

        requestMappingInfoHandlerMethodMap.values().forEach(m -> {
            if (!ArrayUtils.contains(integrationTestGenerator.ignoreClasses(), m.getBeanType())) {
                resourceClasses.add(m.getBeanType());
            }
        });


        for (Class<?> resourceClass : resourceClasses) {
            generateSingleIntegrationTestClass(requestMappingInfoHandlerMethodMap, integrationTestGenerator, resourceClass);
        }
    }

    private void generateSingleIntegrationTestClass(Map<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethodMap, com.beyt.generator.annotation.IntegrationTestGenerator integrationTestGenerator, Class<?> resourceClass) {
        Map<Class<?>, String> autowireClassMap = new HashMap<>();
        Set<Class<?>> importClasses = new HashSet<>();
        clearVariableMap();
        fillDefaultVariables(integrationTestGenerator, resourceClass, importClasses, autowireClassMap);
        variableMap.get(eIntegrationTestVariable.CLASSNAME).append(resourceClass.getSimpleName());
        Map<String, Integer> methodNameUniquenessMap = new HashMap<>();
        requestMappingInfoHandlerMethodMap.forEach((requestMappingInfo, handlerMethod) -> {
            if (!handlerMethod.getBeanType().equals(resourceClass)) {
                return;
            }


            if (handlerMethod.getMethod().getParameterTypes().length > 0) {
                createAutowiredClassesAndClassFieldStorage(autowireClassMap, handlerMethod, resourceClass);
            }
            variableMap.get(eIntegrationTestVariable.BEFORE_EACH);
            String methodNamePostFix = prepareMethodNamePostfix(methodNameUniquenessMap, handlerMethod);
            variableMap.get(eIntegrationTestVariable.CLASS_BODY).append(generateTestMethod(requestMappingInfo, handlerMethod, importClasses, resourceClass, methodNamePostFix, integrationTestGenerator));
        });

        importClasses.addAll(autowireClassMap.keySet());
        importClasses.forEach(c -> variableMap.get(eIntegrationTestVariable.IMPORTS).append("import ").append(c.getName()).append(";\n"));
        IntegrationTestTemplateHelper.generateAutowireVariable(autowireClassMap, variableMap);

        String result = IntegrationTestTemplateHelper.generateTemplate("/integration_test/IntegrationTest.tempjava", (Map<ITemplateVariableEnum, CharSequence>) (Map<?, ?>) variableMap);
        IntegrationTestTemplateHelper.writeFile(result, resourceClass.getSimpleName() + "IT.java", integrationTestGenerator.outputPath());
        log.info(result);
        log.info("END " + resourceClass.getName());
    }

    private String prepareMethodNamePostfix(Map<String, Integer> methodNameUniquenessMap, HandlerMethod handlerMethod) {
        methodNameUniquenessMap.computeIfPresent(handlerMethod.getMethod().getName(), (key, value) -> ++value);
        methodNameUniquenessMap.putIfAbsent(handlerMethod.getMethod().getName(), 0);
        Integer integer = methodNameUniquenessMap.get(handlerMethod.getMethod().getName());
        return integer == 0 ? "" : integer.toString();
    }

    private void generateTestUtilClass(com.beyt.generator.annotation.IntegrationTestGenerator integrationTestGenerator) {
        Map<ITemplateVariableEnum, CharSequence> testUtilMap = new HashMap<>();
        testUtilMap.put(eIntegrationTestVariable.PACKAGE, integrationTestGenerator.packageForTest());
        String result = IntegrationTestTemplateHelper.generateTemplate("/integration_test/TestUtil.tempjava", testUtilMap);
        IntegrationTestTemplateHelper.writeFile(result, "TestUtil.java", integrationTestGenerator.outputPath());
    }

    private void fillDefaultVariables(com.beyt.generator.annotation.IntegrationTestGenerator integrationTestGenerator, Class<?> resourceClass, Set<Class<?>> importClasses, Map<Class<?>, String> autowireClassMap) {
        importClasses.add(integrationTestGenerator.mainClass());
        importClasses.add(resourceClass);
        autowireClassMap.put(EntityManager.class, generateAutowireStr(EntityManager.class));
        variableMap.get(eIntegrationTestVariable.PACKAGE).append(integrationTestGenerator.packageForTest());
        variableMap.get(eIntegrationTestVariable.SPRING_START_CLASS).append(integrationTestGenerator.mainClass().getSimpleName());
        variableMap.get(eIntegrationTestVariable.TODAY).append(DATE_FORMAT.format(new Date()));
    }

    private void clearVariableMap() {
        variableMap.forEach((k, v) -> v.setLength(0));
    }

    private void createAutowiredClassesAndClassFieldStorage(Map<Class<?>, String> autowireClassMap, HandlerMethod handlerMethod, Class<?> resourceClass) {
        Method method = handlerMethod.getMethod();
        Set<Class<?>> methodClasses = new HashSet<>(Arrays.asList(method.getParameterTypes()));
        methodClasses.add(GenericTypeResolverExtendedUtil.getMethodDeepestReturnType(method, ResponseEntity.class));
        resourceFieldStorage.putIfAbsent(resourceClass, new HashMap<>());
        for (Class<?> referenceClass : methodClasses) {
            classFieldStorage.putIfAbsent(referenceClass, new HashMap<>());
            if (ReflectionUtil.isContainsAllTypeInTwoGenericTypeResults(referenceClass, entityMapperGenericTypeResults)
                    || ReflectionUtil.isContains1TypeInTwoGenericTypeResults(referenceClass, classJpaRepositoryMap)) {
                EntityMapper objectWithType = ReflectionUtil.findObjectWithType1(referenceClass, entityMapperGenericTypeResults);
                if (objectWithType != null) {
                    Class<?> entityMapperClass = checkBean(objectWithType);
                    autowireClassMap.putIfAbsent(entityMapperClass, generateAutowireStr(entityMapperClass));
                    classFieldStorage.get(referenceClass).putIfAbsent(eUsageClassType.ObjectMapper, Pair.of(entityMapperClass, StringUtil.lowerFirstLetter(entityMapperClass.getSimpleName())));
                    resourceFieldStorage.get(resourceClass).put(eUsageClassType.ObjectMapper, Pair.of(entityMapperClass, StringUtil.lowerFirstLetter(entityMapperClass.getSimpleName())));
                }
                Class<?> entityClass = ReflectionUtil.findType2WithType1(referenceClass, entityMapperGenericTypeResults);
                if (entityClass != null) {
                    entityClass = checkClass(entityClass);
                    classFieldStorage.get(referenceClass).putIfAbsent(eUsageClassType.Entity, Pair.of(entityClass, StringUtil.lowerFirstLetter(entityClass.getSimpleName())));
                    classFieldStorage.get(referenceClass).putIfAbsent(eUsageClassType.EntityDto, Pair.of(checkClass(referenceClass), StringUtil.lowerFirstLetter(checkClass(referenceClass).getSimpleName())));
                    resourceFieldStorage.get(resourceClass).put(eUsageClassType.Entity, Pair.of(entityClass, StringUtil.lowerFirstLetter(entityClass.getSimpleName())));
                    resourceFieldStorage.get(resourceClass).put(eUsageClassType.EntityDto, Pair.of(checkClass(referenceClass), StringUtil.lowerFirstLetter(checkClass(referenceClass).getSimpleName())));
                    JpaRepository jpaRepository = ReflectionUtil.findObjectWithType1(entityClass, classJpaRepositoryMap);
                    if (jpaRepository != null) {
                        Class<?> jpaRepositoryClass = checkBean(jpaRepository);
                        autowireClassMap.putIfAbsent(jpaRepositoryClass, generateAutowireStr(jpaRepositoryClass));
                        classFieldStorage.get(referenceClass).putIfAbsent(eUsageClassType.Repository, Pair.of(jpaRepositoryClass, StringUtil.lowerFirstLetter(jpaRepositoryClass.getSimpleName())));
                        resourceFieldStorage.get(resourceClass).put(eUsageClassType.Repository, Pair.of(jpaRepositoryClass, StringUtil.lowerFirstLetter(jpaRepositoryClass.getSimpleName())));
                    }
                }
                JpaRepository jpaRepository = ReflectionUtil.findObjectWithType1(referenceClass, classJpaRepositoryMap);
                if (jpaRepository != null) {
                    Class<?> jpaRepositoryClass = checkBean(jpaRepository);
                    autowireClassMap.putIfAbsent(jpaRepositoryClass, generateAutowireStr(jpaRepositoryClass));
                    classFieldStorage.get(referenceClass).putIfAbsent(eUsageClassType.Repository, Pair.of(jpaRepositoryClass, StringUtil.lowerFirstLetter(jpaRepositoryClass.getSimpleName())));
                    classFieldStorage.get(referenceClass).putIfAbsent(eUsageClassType.Entity, Pair.of(checkClass(referenceClass), StringUtil.lowerFirstLetter(checkClass(referenceClass).getSimpleName())));
                    resourceFieldStorage.get(resourceClass).put(eUsageClassType.Repository, Pair.of(jpaRepositoryClass, StringUtil.lowerFirstLetter(jpaRepositoryClass.getSimpleName())));
                    resourceFieldStorage.get(resourceClass).put(eUsageClassType.Entity, Pair.of(checkClass(referenceClass), StringUtil.lowerFirstLetter(checkClass(referenceClass).getSimpleName())));
                }
            }

            if (classFieldStorage.get(referenceClass).isEmpty()) {
                classFieldStorage.remove(referenceClass);
            }
        }
    }

    public String generateTestMethod(RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod, Set<Class<?>> importClasses, Class<?> resourceClass, String methodNamePostFix, com.beyt.generator.annotation.IntegrationTestGenerator integrationTestGenerator) {
        String result = null;
        Map<eIntegrationTestMethodVariable, String> templateValueMap = new HashMap<>();
        templateValueMap.put(eIntegrationTestMethodVariable.MethodName, handlerMethod.getMethod().getName() + methodNamePostFix);
        // Create Method (EntityDTO dto)
        try {
            result = integrationTestMethodGenerator.methodCreate(resourceClass, requestMappingInfo, handlerMethod, importClasses, (Map<ITemplateVariableEnum, CharSequence>) (Map<?, ?>) templateValueMap, integrationTestGenerator);
        } catch (Exception e) {
            templateValueMap.put(eIntegrationTestMethodVariable.WarningMessage, "Warn: While Create Method generation exception thrown!: " + e.getMessage() + " " + ExceptionUtil.getPrintStackTrace(e));
        }

        if (StringUtil.isNullOrEmpty(result)) {
            if (!templateValueMap.containsKey(eIntegrationTestMethodVariable.WarningMessage)) {
                templateValueMap.put(eIntegrationTestMethodVariable.WarningMessage, "Warn: Available method generate Type not found!");
            }
            result = IntegrationTestTemplateHelper.generateTemplate("/integration_test/methods/EmptyITMethod.tempjava", (Map<ITemplateVariableEnum, CharSequence>) (Map<?, ?>) templateValueMap);
        }

        return result;
    }

    public boolean isEntity(Class<?> clazz) {
        return entityManager.getMetamodel().getEntities().stream().anyMatch(e -> e.getJavaType().isAssignableFrom(clazz));
    }

    public boolean isEntityDTO(Class<?> clazz) {
        //return classFieldStorage.entrySet().stream().anyMatch((entity) -> entity.getValue().entrySet().stream().anyMatch((e) -> e.getValue().getFirst().isAssignableFrom(clazz)));
        return ReflectionUtil.isContains1TypeInTwoGenericTypeResults(clazz, entityMapperGenericTypeResults);
    }

    private Class<?> checkBean(Object object) {
        Class<?> clazz = checkClass(object.getClass());

        return (AopUtils.isCglibProxy(object) ? clazz.getSuperclass() : clazz);
    }

    private Class<?> checkClass(Class<?> clazz) {
        if (Proxy.isProxyClass(clazz)) {
            clazz = clazz.getInterfaces()[0];
        }
        return clazz;
    }

    private String generateAutowireStr(Class<?> objectWithType) {
        Assert.notNull(objectWithType, "Class must not be null");
        String simpleName = objectWithType.getSimpleName();
        return "    @Autowired\n    " + simpleName + " " + StringUtil.lowerFirstLetter(simpleName) + ";";
    }

    public Map<eIntegrationTestVariable, StringBuilder> getVariableMap() {
        return variableMap;
    }

    public List<ReflectionUtil.TwoGenericTypeResult<JpaRepository<?, ?>>> getClassJpaRepositoryMap() {
        return classJpaRepositoryMap;
    }

    public Map<Class<?>, Map<eUsageClassType, Pair<Class<?>, String>>> getClassFieldStorage() {
        return classFieldStorage;
    }

    public Map<Class<?>, Map<eUsageClassType, Pair<Class<?>, String>>> getResourceFieldStorage() {
        return resourceFieldStorage;
    }

    @Autowired
    @Lazy
    public void setIntegrationTestMethodGenerator(IntegrationTestMethodGenerator integrationTestMethodGenerator) {
        this.integrationTestMethodGenerator = integrationTestMethodGenerator;
    }
}
