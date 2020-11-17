/**
 * Copyright © 2018 organization baomidou
 * <pre>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <pre/>
 */
package com.baomidou.dynamic.datasource.creator;

import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.baomidou.dynamic.datasource.enums.SeataMode;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.dynamic.datasource.support.ScriptRunner;
import com.p6spy.engine.spy.P6DataSource;
import io.seata.rm.datasource.DataSourceProxy;
import io.seata.rm.datasource.xa.DataSourceProxyXA;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;

/**
 * 数据源创建器
 *
 * @author TaoYu
 * @since 2.3.0
 */
@Slf4j
@Setter
public class CompositeDataSourceCreator implements DataSourceCreator {

    private DynamicDataSourceProperties properties;
    private List<DataSourceCreator> dataSourceCreator;

    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        return createDataSource(dataSourceProperty, properties.getPublicKey());
    }

    /**
     * 创建数据源
     *
     * @param dataSourceProperty 数据源信息
     * @return 数据源
     */
    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty, String publicKey) {
        DataSourceCreator factory = null;
        for (DataSourceCreator dataSourceCreator : this.dataSourceCreator) {
            if (dataSourceCreator.support(dataSourceProperty)) {
                factory = dataSourceCreator;
                break;
            }
        }
        if (factory == null) {
            throw new IllegalStateException("factory must not be null,please check the DataSourceCreator");
        }
        DataSource dataSource = factory.createDataSource(dataSourceProperty, publicKey);
        this.runScrip(dataSource, dataSourceProperty);
        return wrapDataSource(dataSource, dataSourceProperty);
    }


    private void runScrip(DataSource dataSource, DataSourceProperty dataSourceProperty) {
        String schema = dataSourceProperty.getSchema();
        String data = dataSourceProperty.getData();
        if (StringUtils.hasText(schema) || StringUtils.hasText(data)) {
            ScriptRunner scriptRunner = new ScriptRunner(dataSourceProperty.isContinueOnError(), dataSourceProperty.getSeparator());
            if (StringUtils.hasText(schema)) {
                scriptRunner.runScript(dataSource, schema);
            }
            if (StringUtils.hasText(data)) {
                scriptRunner.runScript(dataSource, data);
            }
        }
    }

    private DataSource wrapDataSource(DataSource dataSource, DataSourceProperty dataSourceProperty) {
        String name = dataSourceProperty.getPoolName();
        DataSource targetDataSource = dataSource;

        Boolean enabledP6spy = properties.getP6spy() && dataSourceProperty.getP6spy();
        if (enabledP6spy) {
            targetDataSource = new P6DataSource(dataSource);
            log.debug("dynamic-datasource [{}] wrap p6spy plugin", name);
        }

        Boolean enabledSeata = properties.getSeata() && dataSourceProperty.getSeata();
        SeataMode seataMode = properties.getSeataMode();
        if (enabledSeata) {
            targetDataSource = SeataMode.XA == seataMode ? new DataSourceProxyXA(dataSource) : new DataSourceProxy(dataSource);
            log.debug("dynamic-datasource [{}] wrap seata plugin transaction mode [{}]", name, seataMode);
        }
        return new ItemDataSource(name, dataSource, targetDataSource, enabledP6spy, enabledSeata, seataMode);
    }


    public void setDataSourceCreatorFactory(List<DataSourceCreator> dataSourceCreator) {
        this.dataSourceCreator = dataSourceCreator;
    }


    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        return true;
    }
}
