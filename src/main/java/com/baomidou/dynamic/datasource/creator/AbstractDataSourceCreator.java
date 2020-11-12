package com.baomidou.dynamic.datasource.creator;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;

import javax.sql.DataSource;

/**
 * 默认创建数据源无参的调用有参的
 * @author ls9527
 */
public abstract class AbstractDataSourceCreator implements DataSourceCreator{


    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        return createDataSource(dataSourceProperty,null);
    }

}
