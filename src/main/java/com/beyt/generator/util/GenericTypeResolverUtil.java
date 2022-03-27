package com.beyt.generator.util;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by tdilber at 11/16/2020
 */
@Slf4j
public final class GenericTypeResolverUtil {
    private GenericTypeResolverUtil() {
    }

    public static GenericsTypesTreeResult resolveClassGenericsTree(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        ResolvableType resolvableType = ResolvableType.forClass(clazz);
        if (!resolvableType.hasGenerics() || resolvableType.getType() instanceof WildcardType) {
            return new GenericsTypesTreeResult(clazz, 0);
        }

        return getGenericsTypesTreeResult(clazz, resolvableType.getGenerics(), 0);
    }

    public static GenericsTypesTreeResult resolveConstructorParameterGenericsTree(Constructor<?> constructor, Integer parameterIndex) {
        Assert.notNull(constructor, "Constructor must not be null");
        Assert.notNull(parameterIndex, "Parameter index must not be null");
        ResolvableType resolvableType = ResolvableType.forConstructorParameter(constructor, parameterIndex);
        if (!resolvableType.hasGenerics() || resolvableType.getType() instanceof WildcardType) {
            return new GenericsTypesTreeResult(constructor.getParameterTypes()[parameterIndex], 0);
        }

        return getGenericsTypesTreeResult(constructor.getParameterTypes()[parameterIndex], resolvableType.getGenerics(), 0);
    }

    public static GenericsTypesTreeResult resolveFieldGenericsTree(Field field) {
        Assert.notNull(field, "Field must not be null");
        ResolvableType resolvableType = ResolvableType.forField(field);
        if (!resolvableType.hasGenerics() || resolvableType.getType() instanceof WildcardType) {
            return new GenericsTypesTreeResult(field.getType(), 0);
        }

        return getGenericsTypesTreeResult(field.getType(), resolvableType.getGenerics(), 0);
    }

    public static GenericsTypesTreeResult resolveReturnTypeGenericsTree(Method method) {
        Assert.notNull(method, "Method must not be null");
        ResolvableType resolvableType = ResolvableType.forMethodReturnType(method).as(method.getReturnType());
        if (!resolvableType.hasGenerics() || resolvableType.getType() instanceof WildcardType) {
            return new GenericsTypesTreeResult(method.getReturnType(), 0);
        }

        return getGenericsTypesTreeResult(method.getReturnType(), resolvableType.getGenerics(), 0);
    }

    public static GenericsTypesTreeResult resolveMethodParameterGenericsTree(Method method, Integer parameterIndex) {
        Assert.notNull(method, "Method must not be null");
        Assert.notNull(parameterIndex, "Parameter index must not be null");
        ResolvableType resolvableType = ResolvableType.forMethodParameter(method, parameterIndex);
        if (!resolvableType.hasGenerics() || resolvableType.getType() instanceof WildcardType) {
            return new GenericsTypesTreeResult(method.getParameterTypes()[parameterIndex], 0);
        }

        return getGenericsTypesTreeResult(method.getParameterTypes()[parameterIndex], resolvableType.getGenerics(), 0);
    }

    private static GenericsTypesTreeResult getGenericsTypesTreeResult(Class<?> returnType, ResolvableType[] generics, int order) {
        GenericsTypesTreeResult result = new GenericsTypesTreeResult(returnType, order);

        for (int i = 0; i < generics.length; i++) {
            ResolvableType generic = generics[i];
            result.getSubClasses().add(getGenericsTypesTreeResult(generic.resolve(), generic.getGenerics(), i));
        }

        return result;
    }


    @Getter
    @Setter
    public static class GenericsTypesTreeResult {
        private Integer order;
        private Class<?> clazz;
        private List<GenericsTypesTreeResult> subClasses = new ArrayList<>();

        public GenericsTypesTreeResult(Class<?> clazz, Integer order) {
            this.order = order;
            this.clazz = clazz;
        }

        public String printResult() {
            if (CollectionUtils.isEmpty(subClasses)) {
                return clazz.getSimpleName();
            }

            return clazz.getSimpleName()
                    + "<"
                    + subClasses.stream().map(GenericsTypesTreeResult::printResult).collect(Collectors.joining(", "))
                    + ">";
        }

        public List<Class<?>> getAllClasses() {
            ArrayList<Class<?>> result = new ArrayList<>();
            getAllClassesRecursiveMethod(result);
            return result;
        }

        private void getAllClassesRecursiveMethod(List<Class<?>> currentList) {
            currentList.add(clazz);
            if (!CollectionUtils.isEmpty(subClasses)) {
                subClasses.forEach(sb -> sb.getAllClassesRecursiveMethod(currentList));
            }
        }
    }


    @Nullable
    public static Class<?> resolveReturnTypeDeepestArgument(Method method, Class<?> genericIfc) {
        Assert.notNull(method, "Method must not be null");
        ResolvableType resolvableType = ResolvableType.forMethodReturnType(method).as(genericIfc);
        if (!resolvableType.hasGenerics() || resolvableType.getType() instanceof WildcardType) {
            return null;
        }
        return getDeepestGeneric(resolvableType);
    }

    @Nullable
    public static Class<?> getDeepestGeneric(ResolvableType resolvableType) {
        Assert.isTrue(resolvableType.getGenerics().length == 1,
                () -> "Expected 1 type argument on generic interface [" + resolvableType +
                        "] but found " + resolvableType.getGenerics().length);
        ResolvableType generic = resolvableType.getGeneric();
        while (!generic.toString().equals("?")) {
            if (!generic.getGeneric().toString().equals("?")) {
                generic = generic.getGeneric();
            } else {
                break;
            }
        }
        return generic.resolve();
    }

    public static Class<?> getMethodDeepestReturnType(Method method, Class<?> genericClass) {
        Class<?> methodClasses = null;
        if (!method.getReturnType().getName().equals("void")) {
            if (genericClass.isAssignableFrom(method.getReturnType())) {
                Class<?> genericDeepestClass = resolveReturnTypeDeepestArgument(method, genericClass);

                if (genericDeepestClass != null) {
                    methodClasses = genericDeepestClass;
                }
            }
        }
        if (methodClasses == null) {
            methodClasses = method.getReturnType();
        }

        return methodClasses;
    }

    public static List<Class<?>> getMethodAllReturnTypes(Method method) {
        List<Class<?>> result = new ArrayList<>();
        result.add(method.getReturnType());
        result.addAll(getMethodAllReturnTypes(method, method.getReturnType()));
        return result;
    }

    public static List<Class<?>> getMethodAllReturnTypes(Method method, Class<?> genericClass) {
        List<Class<?>> methodClasses = Collections.emptyList();
        if (!method.getReturnType().getName().equals("void")) {
            if (genericClass.isAssignableFrom(method.getReturnType())) {
                List<Class<?>> genericDeepestClass = resolveReturnTypeAllArguments(method, genericClass);

                if (!CollectionUtils.isEmpty(genericDeepestClass)) {
                    methodClasses = genericDeepestClass;
                }
            }
        }

        return methodClasses;
    }

    public static List<Class<?>> resolveReturnTypeAllArguments(Method method, Class<?> genericIfc) {
        Assert.notNull(method, "Method must not be null");
        ResolvableType resolvableType = ResolvableType.forMethodReturnType(method).as(genericIfc);
        if (!resolvableType.hasGenerics() || resolvableType.getType() instanceof WildcardType) {
            return null;
        }
        Assert.isTrue(resolvableType.getGenerics().length == 1,
                () -> "Expected 1 type argument on generic interface [" + resolvableType +
                        "] but found " + resolvableType.getGenerics().length);
        ResolvableType generic = resolvableType.getGeneric();
        List<Class<?>> result = new ArrayList<>();
        while (!generic.toString().equals("?")) {
            result.add(generic.resolve());
            if (!generic.getGeneric().toString().equals("?")) {
                generic = generic.getGeneric();
            } else {
                break;
            }
        }
        return result;
    }
}
