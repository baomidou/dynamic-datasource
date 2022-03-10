package com.baomidou.dynamic.datasource.provider;

import javax.sql.DataSource;

/**
 * Created on 2022/3/10.
 *
 * @author heyangya
 */
public class DefaultRuntimeDataSourceProvider implements RuntimeDataSourceProvider {
    /**
     * 实时获取数据源
     *
     * @param key 数据源名称
     */
    @Override
    public DataSource getDataSource(String key) {
        return null;
    }
}
