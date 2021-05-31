package com.baomidou.dynamic.datasource.spring.boot.autoconfigure;


public interface DynamicDataSourcePropertiesCustomizer {

    /**
     * Customize the given a {@link DynamicDataSourceProperties} object.
     *
     * @param properties the DynamicDataSourceProperties object to customize
     */
    void customize(DynamicDataSourceProperties properties);
}
