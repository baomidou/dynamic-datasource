/*
 * Copyright © 2018 organization baomidou
 *
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
 */
package com.baomidou.dynamic.datasource.creator;

import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.baomidou.dynamic.datasource.enums.SeataMode;
import com.baomidou.dynamic.datasource.event.DataSourceInitEvent;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DatasourceInitProperties;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.dynamic.datasource.support.ScriptRunner;
import com.p6spy.engine.spy.P6DataSource;
import io.seata.rm.datasource.DataSourceProxy;
import io.seata.rm.datasource.xa.DataSourceProxyXA;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * 抽象连接池创建器
 * <p>
 * 这里主要处理一些公共逻辑，如脚本和事件等
 *
 * @author TaoYu
 */
@Slf4j
public abstract class AbstractDataSourceCreator implements DataSourceCreator {

    @Autowired
    protected DynamicDataSourceProperties properties;
    @Autowired
    protected DataSourceInitEvent dataSourceInitEvent;

    /**
     * 子类去实际创建连接池
     *
     * @param dataSourceProperty 数据源信息
     * @return 实际连接池
     */
    public abstract DataSource doCreateDataSource(DataSourceProperty dataSourceProperty);

    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        String publicKey = dataSourceProperty.getPublicKey();
        if (StringUtils.isEmpty(publicKey)) {
            publicKey = properties.getPublicKey();
            dataSourceProperty.setPublicKey(publicKey);
        }
        Boolean lazy = dataSourceProperty.getLazy();
        if (lazy == null) {
            lazy = properties.getLazy();
            dataSourceProperty.setLazy(lazy);
        }
        dataSourceInitEvent.beforeCreate(dataSourceProperty);
        DataSource dataSource = doCreateDataSource(dataSourceProperty);
        dataSourceInitEvent.afterCreate(dataSource);
        this.runScrip(dataSource, dataSourceProperty);
        return wrapDataSource(dataSource, dataSourceProperty);
    }

    private void runScrip(DataSource dataSource, DataSourceProperty dataSourceProperty) {
        DatasourceInitProperties initProperty = dataSourceProperty.getInit();
        String schema = initProperty.getSchema();
        String data = initProperty.getData();
        if (StringUtils.hasText(schema) || StringUtils.hasText(data)) {
            ScriptRunner scriptRunner = new ScriptRunner(initProperty.isContinueOnError(), initProperty.getSeparator());
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
            if (SeataMode.XA == seataMode) {
                targetDataSource = new DataSourceProxyXA(targetDataSource);
            } else {
                targetDataSource = new DataSourceProxy(targetDataSource);
            }
            log.debug("dynamic-datasource [{}] wrap seata plugin transaction mode ", name);
        }
        return new ItemDataSource(name, dataSource, targetDataSource, enabledP6spy, enabledSeata, seataMode);
    }
}
