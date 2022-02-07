package com.beyt.generator.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.beyt.generator.util.field.FieldUtil;
import lombok.extern.slf4j.Slf4j;
import net.jodah.typetools.TypeResolver;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.GenericTypeResolver;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

/**
 * Created by tdilber at 7/10/2020
 */
@Slf4j
public class ReflectionUtil {
    private static Gson gson = new GsonBuilder()
            .setLenient()
            .create();
    private static Random random = new Random();


    public static void convertObjectArrayToIfNotAvailable(Class<?> clazz, Object[] objects) throws Exception {
        convertObjectArrayToIfEnum(clazz, objects);
        convertObjectArrayToIfDate(clazz, objects);
    }

    public static void convertObjectArrayToIfEnum(Class<?> clazz, Object[] objects) throws Exception {
        if (objects.length > 0 && clazz.isEnum()) {
            Method valueOf = clazz.getMethod(
                    "valueOf", String.class);
            for (int i = 0; i < objects.length; i++) {
                objects[i] = valueOf.invoke(null, objects[i].toString());
            }
        }
    }

    public static void convertObjectArrayToIfDate(Class<?> clazz, Object[] objects) throws Exception {
        if (objects.length > 0 && Date.class.isAssignableFrom(clazz)) {
            for (int i = 0; i < objects.length; i++) {
                objects[i] = new Date(Long.parseLong(objects[i].toString()));
            }
        }
    }

    public static Class<?> getGenericTypeArgument(Class<?> clazzWithGenericArgument, Class<?> clazzWithoutGenericArgument) {
        return GenericTypeResolver.resolveTypeArgument(clazzWithGenericArgument, clazzWithoutGenericArgument);
    }

    public static Object convertStringObjectByType(Class<?> parameterType, String valueStr) {
        return gson.fromJson(valueStr, parameterType);
    }

    public static void fillParametersRandom(Object object, Class<?> clazz, int index) {
        try {
            for (PropertyDescriptor property : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                Method method = property.getWriteMethod();
                String fieldName = property.getName();

                if (method != null) {
                    Object field = FieldUtil.fillRandom(method.getParameterTypes()[0]);

                    if (field != null) {
                        method.invoke(object, field);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static <T> List<T> getAvailableClassesInstanceInDirectory(String packageName, Class<T> tClass) {
        List<T> list = new ArrayList<>();
        try {
            Class[] classes = getClasses(packageName);
            for (Class c : classes) {
                if (Modifier.isAbstract(c.getModifiers()))
                    continue;
                if (tClass.isAssignableFrom(c)) {
                    T d = (T) c.newInstance();
                    if (d != null)
                        list.add(d);
                }
            }
        } catch (ClassNotFoundException | IOException | InstantiationException |
                IllegalAccessException ex) {
            log.error(ex.getMessage(), ex);
        }
        return list;
    }

    public static <T> List<Class<T>> getAvailableClassesInDirectory(String packageName, Class<T> tClass) {
        List<Class<T>> list = new ArrayList<>();
        try {
            Class[] classes = getClasses(packageName);
            for (Class c : classes) {
                if (Modifier.isAbstract(c.getModifiers()))
                    continue;
                if (tClass.isAssignableFrom(c)) {
                    list.add(c);
                }
            }
        } catch (ClassNotFoundException | IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        return list;
    }


    /**
     * Scans all classes accessible from the context class loader which belong
     * to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static Class[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and
     * subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base
     *                    directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List findClasses(File directory, String packageName) throws
            ClassNotFoundException {
        List classes = new ArrayList();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' +
                        file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    public static <T extends Enum<T>> T generateCustomEnumValue(Class<T> clazz, int ordinal) {
        T[] enumConstants = clazz.getEnumConstants();

        return enumConstants[ordinal % enumConstants.length];
    }

    public static List<Object[]> objectListToObjectFieldsList(Class<?> entityClass, List<?> objectList) throws IllegalAccessException {
        List<Object[]> result = new ArrayList<>();
        List<Field> declaredFieldList = getDeclaredFieldList(entityClass);

        for (Object o : objectList) {
            Object[] resultElement = new Object[declaredFieldList.size()];
            for (int i = 0; i < declaredFieldList.size(); i++) {
                resultElement[i] = declaredFieldList.get(i).get(o);
            }
            result.add(resultElement);
        }

        return result;
    }

    public static List<Object> convertObjectToObjectFieldList(Object object) throws IllegalAccessException {

        Class<?> clazz = object.getClass();

        List<Field> declaredFieldList = getDeclaredFieldList(clazz);

        return convertObjectToObjectFieldList(object, declaredFieldList);
    }

    public static List<Object> convertObjectToObjectFieldList(Object object, List<Field> declaredFieldList) throws IllegalAccessException {
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < declaredFieldList.size(); i++) {
            result.add(declaredFieldList.get(i).get(object));
        }
        return result;
    }

    public static List<Field> getDeclaredFieldList(Class<?> clazz) {
        List<Field> declaredFieldList = new ArrayList<>();

        Field[] declaredFields = clazz.getDeclaredFields();

        for (Field declaredField : declaredFields) {
            if (!Modifier.isStatic(declaredField.getModifiers())) {
                declaredField.setAccessible(true);
                declaredFieldList.add(declaredField);
            }
        }
        return declaredFieldList;
    }

//    @SneakyThrows
//    public static <Bean> Map<Class<?>, Bean> listToMap(List<Bean> beanList, Class<?> genericType) {
//        Map<Class<?>, Bean> beanHashMap = new HashMap<>();
//        TypeResolver resolver = new TypeResolver().where(genericType, genericType);
//        for (Bean bean : beanList) {
//            Class<?> aClass = getTargetObject(bean).getClass();
//            Type type = new TypeResolver().where(genericType, aClass).resolveType(aClass);

//    Class<?> objClz = obj.getClass();
//            if (org.springframework.aop.support.AopUtils.isAopProxy(obj)) {
//        objClz = org.springframework.aop.support.AopUtils.getTargetClass(obj);
//    }

//            Class<?> typeArg = com.google.common.reflect.TypeToken.of(type).getRawType();
//            beanHashMap.put(typeArg, bean);
//            log.trace(typeArg.toString() + " entity typed " + genericType.getSimpleName() + " detected!");
//        }
//
//        return beanHashMap;
//    }

    public static <Bean> Map<Class<?>, Bean> genericListToTypeObjectMap(List<Bean> beanList, Class<?> genericType) {
        Map<Class<?>, Bean> beanHashMap = new HashMap<>();
        for (Bean bean : beanList) {
            Class<?> typeArg = TypeResolver.resolveRawArgument(genericType, bean.getClass());
            beanHashMap.put(typeArg, bean);
            log.trace(typeArg.toString() + " entity typed " + genericType.getSimpleName() + " detected!");
        }

        return beanHashMap;
    }

    public static <Bean> List<TwoGenericTypeResult<Bean>> genericListToType2ObjectMap(List<Bean> beanList, Class<?> genericType) {
        List<TwoGenericTypeResult<Bean>> result = new ArrayList<>();
        for (Bean bean : beanList) {
            Class<?>[] classes = TypeResolver.resolveRawArguments(genericType, bean.getClass());
            result.add(new TwoGenericTypeResult<Bean>(classes[0], classes[1], bean));
            log.trace(classes[0].toString() + " entity typed " + classes[1].toString() + " entity typed " + genericType.getSimpleName() + " detected!");
        }

        return result;
    }

    public static <T> boolean isContainsAllTypeInTwoGenericTypeResults(Class<?> clazz, List<TwoGenericTypeResult<T>> twoGenericTypeResults) {
        for (TwoGenericTypeResult<?> twoGenericTypeResult : twoGenericTypeResults) {
            if (twoGenericTypeResult.type1.equals(clazz) || twoGenericTypeResult.type2.equals(clazz)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean isContains1TypeInTwoGenericTypeResults(Class<?> clazz, List<TwoGenericTypeResult<T>> twoGenericTypeResults) {
        for (TwoGenericTypeResult<?> twoGenericTypeResult : twoGenericTypeResults) {
            if (twoGenericTypeResult.type1.equals(clazz)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean isContains2TypeInTwoGenericTypeResults(Class<?> clazz, List<TwoGenericTypeResult<T>> twoGenericTypeResults) {
        for (TwoGenericTypeResult<?> twoGenericTypeResult : twoGenericTypeResults) {
            if (twoGenericTypeResult.type2.equals(clazz)) {
                return true;
            }
        }
        return false;
    }

    public static <T> T findObjectWithType2(Class<?> entityClass, List<TwoGenericTypeResult<T>> twoGenericTypeResults) {
        T result = null;
        for (TwoGenericTypeResult<T> twoGenericTypeResult : twoGenericTypeResults) {
            if (twoGenericTypeResult.type2.equals(entityClass)) {
                result = twoGenericTypeResult.object;
            }
        }

        return result;
    }

    public static <T> T findObjectWithType1(Class<?> entityClass, List<TwoGenericTypeResult<T>> twoGenericTypeResults) {
        T result = null;
        for (TwoGenericTypeResult<T> twoGenericTypeResult : twoGenericTypeResults) {
            if (twoGenericTypeResult.type1.equals(entityClass)) {
                result = twoGenericTypeResult.object;
            }
        }

        return result;
    }

    public static <T> Class<?> findType1WithType2(Class<?> entityClass, List<TwoGenericTypeResult<T>> twoGenericTypeResults) {
        Class<?> result = null;
        for (TwoGenericTypeResult<T> twoGenericTypeResult : twoGenericTypeResults) {
            if (twoGenericTypeResult.type2.equals(entityClass)) {
                result = twoGenericTypeResult.type1;
            }
        }

        return result;
    }

    public static <T> Class<?> findType2WithType1(Class<?> entityClass, List<TwoGenericTypeResult<T>> twoGenericTypeResults) {
        Class<?> result = null;
        for (TwoGenericTypeResult<T> twoGenericTypeResult : twoGenericTypeResults) {
            if (twoGenericTypeResult.type1.equals(entityClass)) {
                result = twoGenericTypeResult.type2;
            }
        }

        return result;
    }

    public static class TwoGenericTypeResult<T> {
        public Class<?> type1;
        public Class<?> type2;
        public T object;

        public TwoGenericTypeResult(Class<?> type1, Class<?> type2, T object) {
            this.type1 = type1;
            this.type2 = type2;
            this.object = object;
        }
    }


    @SuppressWarnings({"unchecked"})
    public static <T> T getTargetObject(Object object) throws Exception {
        if (AopUtils.isJdkDynamicProxy(object)) {
            return (T) ((Advised) object).getTargetSource().getTarget();
        } else {
            return (T) object; // expected to be cglib object then, which is simply a specialized class
        }
    }
}
