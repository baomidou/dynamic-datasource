package com.baomidou.dynamic.datasource.provider;

import javax.sql.DataSource;

/**
 * Created on 2022/3/9.
 *
 * @author heyangya
 */
public interface RuntimeDataSourceProvider {
    /**
     * 实时获取数据源
     *
     * @param key 数据源名称
     */
    DataSource getDataSource(String key);
}
