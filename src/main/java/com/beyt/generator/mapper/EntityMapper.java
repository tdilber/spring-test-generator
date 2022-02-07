package com.beyt.generator.mapper;

import java.util.List;

/**
 * Contract for a generic dto to entity mapper.
 *
 * @param <D> - DTO type parameter.
 * @param <E> - Entity type parameter.
 */

public interface EntityMapper<D, E> extends Mapper<D, E> {

    E toEntity(D dto);

    List<E> toEntity(List<D> dtoList);

    List<D> toDto(List<E> entityList);
}
