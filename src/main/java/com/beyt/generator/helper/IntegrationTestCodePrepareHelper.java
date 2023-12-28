package com.beyt.generator.helper;

import com.beyt.generator.util.field.FieldUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.beans.Introspector;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tdilber at 11/6/2020
 */
@Slf4j
public final class IntegrationTestCodePrepareHelper {
    private IntegrationTestCodePrepareHelper() {
    }

    public static void settersForObject(String variableName, Class<?> clazz, Object object, Set<Class<?>> importClasses, StringBuilder setsSB) {
        if (object instanceof Map map) {
            setsSB.append("        ").append(clazz.getSimpleName()).append(" ").append(variableName).append(" = new HashMap<>();").append("\n");
            map.entrySet().forEach((e) -> {
                var entry = (Map.Entry) e;
                assertsForObject(variableName + ".put(\"" + entry.getKey() + "\")", entry.getValue().getClass(), entry.getValue(), importClasses, setsSB);
            });
        } else if (!FieldUtil.isSupportedType(clazz)) {
            setsSB.append("        ").append(clazz.getSimpleName()).append(" ").append(variableName).append(" = new ").append(clazz.getSimpleName()).append("();").append("\n");
            for (PropertyValues propertyDescriptor : getPropertyDescriptors(clazz, object)) {
                settersForObject(variableName + "." + propertyDescriptor.getSetterMethod() + "(", propertyDescriptor.getClazz(), propertyDescriptor.getValue(), importClasses, setsSB);
            }
        } else {
            importClasses.add(clazz);
            setters(setsSB, variableName, clazz, FieldUtil.createGeneratorCode(clazz, object.toString()));
        }
    }

    public static void assertsForObject(String variableName, Class<?> clazz, Object object, Set<Class<?>> importClasses, StringBuilder assertsSB) {
        if (object instanceof Map map) {
            map.entrySet().forEach((e) -> {
                var entry = (Map.Entry) e;
                assertsForObject(variableName + ".get(\"" + entry.getKey() + "\")", entry.getValue().getClass(), entry.getValue(), importClasses, assertsSB);
            });
        } else if (!FieldUtil.isSupportedType(clazz)) {
            for (PropertyValues propertyDescriptor : getPropertyDescriptors(clazz, object)) {
                assertsForObject(variableName + "." + propertyDescriptor.getGetterMethod() + "()", propertyDescriptor.getClazz(), propertyDescriptor.getValue(), importClasses, assertsSB);
            }
        } else {
            importClasses.add(clazz);
            asserts(assertsSB, variableName, FieldUtil.createGeneratorCode(clazz, object.toString()));
        }
    }

    public static void settersWithGetter(StringBuilder setsSB, String variableName, String methodName, String valueStr) {
        setsSB.append("        ").append(variableName).append(".")
                .append(methodName).append("(").append(valueStr).append(");\n");
    }

    public static void assertsWithGetter(StringBuilder assertsSB, String variableName, String methodName, String valueStr) {
        assertsSB.append("        assertThat(").append(variableName).append(".").append(methodName)
                .append("()).isEqualTo(").append(valueStr).append(");\n");
    }

    public static void setters(StringBuilder setsSB, String variableName, Class<?> parameterType, String value) {
        if (variableName.endsWith("(")) {
            setsSB.append("        ").append(variableName).append(value).append(");\n");
        } else {
            setsSB.append("        ").append(parameterType.getSimpleName()).append(" ").append(variableName).append(" = ").append(value).append(";\n");
        }
    }

    public static void asserts(StringBuilder assertsSB, String variableName, String value) {
        assertsSB.append("        assertThat(").append(variableName).append(").isEqualTo(").append(value).append(");\n");
    }

    @SneakyThrows
    private static List<PropertyValues> getPropertyDescriptors(Class<?> parameterType, Object parameterObject) {
        return Arrays.stream(Introspector.getBeanInfo(parameterType).getPropertyDescriptors()).filter(propertyDescriptor -> {
            try {
                return propertyDescriptor.getReadMethod() != null && propertyDescriptor.getWriteMethod() != null && !propertyDescriptor.getReadMethod().getName().equals("getClass")
                        && propertyDescriptor.getReadMethod().invoke(parameterObject) != null; //&& !propertyDescriptor.getReadMethod().getName().equals("getId")
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

    public static void requestParam(StringBuilder paramsSB, String variableName) {
        paramsSB.append("                        .param(\"").append(variableName).append("\", ").append(variableName).append(".toString())\n");
    }

    public static void headerParam(StringBuilder headersSB, String headerName) {
        headersSB.append("                        .header(\"").append(headerName).append("\", ").append(headerName).append(")\n");
    }

    public static void headerParam(StringBuilder headersSB, String headerName, String headerValue) {
        headersSB.append("                        .header(\"").append(headerName).append("\", \"").append(headerValue).append("\")\n");
    }

    public static void requestBody(StringBuilder requestBodySB, String variableName) {
        requestBodySB.append("                        .content(TestUtil.convertObjectToJsonBytes(").append(variableName).append("))");
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

    /*
       for (PropertyValues propertyDescriptor : getPropertyDescriptors(clazz, returnObject)) {
            if (!FieldUtil.isSupportedType(propertyDescriptor.getClazz())) {
                assertsSB.append("        new ").append(propertyDescriptor.getClazz().getSimpleName()).append("();\n");
                settersForObject(objectName + "." + propertyDescriptor.getGetterMethod() + "()", propertyDescriptor.getClazz(), propertyDescriptor.getValue(), importClasses, assertsSB);
            } else {
                importClasses.add(propertyDescriptor.getClazz());
                settersWithGetter(assertsSB, objectName, propertyDescriptor.getSetterMethod(), propertyDescriptor.getValueStr());
            }
        }

     */
}
