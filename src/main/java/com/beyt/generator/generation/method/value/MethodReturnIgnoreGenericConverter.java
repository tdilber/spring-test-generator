package com.beyt.generator.generation.method.value;

public interface MethodReturnIgnoreGenericConverter<GenericType, ReturnType> {
    ReturnType convert(GenericType x);

    Class<?> getGenericType();
}
