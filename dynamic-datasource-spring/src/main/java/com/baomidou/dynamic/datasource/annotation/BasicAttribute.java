package com.baomidou.dynamic.datasource.annotation;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * The base kind all dynamicDatasource annotation attribute.
 *
 * @author zp
 * @since 4.1.3
 */
@Data
@AllArgsConstructor
public class BasicAttribute<T> {
    /**
     * dataOperation
     */
    private T dataOperation;
}