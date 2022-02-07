package com.beyt.generator.mapper;

/**
 * Contract for a generic entity to dto mapper.
 *
 * @param <D> - DTO type parameter.
 * @param <E> - Entity type parameter.
 */

public interface Mapper<D, E> {
    D toDto(E entity);
}
