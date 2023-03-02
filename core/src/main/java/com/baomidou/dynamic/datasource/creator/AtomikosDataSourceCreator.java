package com.baomidou.dynamic.datasource.creator;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.atomikos.AtomikosConfig;
import com.baomidou.dynamic.datasource.toolkit.ConfigMergeCreator;
import com.mysql.cj.jdbc.MysqlXADataSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;

import javax.sql.DataSource;

import static com.baomidou.dynamic.datasource.support.DdConstants.ATOMIKOS_DATASOURCE;

/**
 * Atomikos数据源配置
 *
 * @author <a href="mailto:312290710@qq.com">jiazhifeng</a>
 * @date 2023/03/02 10:20
 */
public class AtomikosDataSourceCreator extends AbstractDataSourceCreator implements DataSourceCreator, InitializingBean {
    private static final ConfigMergeCreator<AtomikosConfig, AtomikosConfig> MERGE_CREATOR = new ConfigMergeCreator<>("AtomikosConfig", AtomikosConfig.class, AtomikosConfig.class);
    private AtomikosConfig atomikosConfig;

    @Override
    public DataSource doCreateDataSource(DataSourceProperty dataSourceProperty) {
        AtomikosConfig config = MERGE_CREATOR.create(atomikosConfig, dataSourceProperty.getAtomikos());
        AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();

        MysqlXADataSource mysqlXADataSource = new MysqlXADataSource();
        mysqlXADataSource.setUrl(dataSourceProperty.getUrl());
        mysqlXADataSource.setUser(dataSourceProperty.getUsername());
        mysqlXADataSource.setPassword(dataSourceProperty.getPassword());
        xaDataSource.setXaDataSource(mysqlXADataSource);

        xaDataSource.setUniqueResourceName(dataSourceProperty.getPoolName());
        xaDataSource.setMinPoolSize(config.getMinPoolSize());
        xaDataSource.setMaxPoolSize(config.getMaxPoolSize());
        xaDataSource.setBorrowConnectionTimeout(config.getBorrowConnectionTimeout());
        xaDataSource.setReapTimeout(config.getReapTimeout());
        xaDataSource.setMaxIdleTime(config.getMaxIdleTime());
        xaDataSource.setTestQuery(config.getTestQuery());
        xaDataSource.setMaintenanceInterval(config.getMaintenanceInterval());
        xaDataSource.setDefaultIsolationLevel(config.getDefaultIsolationLevel());
        xaDataSource.setMaxLifetime(config.getMaxLifetime());
        return xaDataSource;
    }

    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        Class<? extends DataSource> type = dataSourceProperty.getType();
        return type == null || ATOMIKOS_DATASOURCE.equals(type.getName());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        atomikosConfig = properties.getAtomikos();
    }
}
