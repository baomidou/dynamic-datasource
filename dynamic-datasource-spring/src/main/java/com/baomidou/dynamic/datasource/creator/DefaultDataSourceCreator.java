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
import com.baomidou.dynamic.datasource.support.ScriptRunner;
import com.baomidou.dynamic.datasource.toolkit.CryptoUtils;
import com.baomidou.dynamic.datasource.toolkit.DsStrUtils;
import com.p6spy.engine.spy.P6DataSource;
import io.seata.rm.datasource.DataSourceProxy;
import io.seata.rm.datasource.xa.DataSourceProxyXA;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
public class DefaultDataSourceCreator {

    private List<DataSourceCreator> creators;

    /**
     * 是否懒加载数据源
     */
    private Boolean lazy = false;
    /**
     * /**
     * 是否使用p6spy输出，默认不输出
     */
    private Boolean p6spy = false;
    /**
     * 是否使用开启seata，默认不开启
     */
    private Boolean seata = false;
    /**
     * seata使用模式，默认AT
     */
    private SeataMode seataMode = SeataMode.AT;
    /**
     * 全局默认publicKey
     */
    private String publicKey = CryptoUtils.DEFAULT_PUBLIC_KEY_STRING;

    private DataSourceInitEvent dataSourceInitEvent;

    /**
     * 创建数据源
     *
     * @param dataSourceProperty 数据源参数
     * @return 数据源
     */
    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        DataSourceCreator dataSourceCreator = null;
        for (DataSourceCreator creator : this.creators) {
            if (creator.support(dataSourceProperty)) {
                dataSourceCreator = creator;
                break;
            }
        }
        if (dataSourceCreator == null) {
            throw new IllegalStateException("creator must not be null,please check the DataSourceCreator");
        }
        String propertyPublicKey = dataSourceProperty.getPublicKey();
        if (DsStrUtils.isEmpty(propertyPublicKey)) {
            dataSourceProperty.setPublicKey(publicKey);
        }
        Boolean propertyLazy = dataSourceProperty.getLazy();
        if (propertyLazy == null) {
            dataSourceProperty.setLazy(lazy);
        }
        if (dataSourceInitEvent != null) {
            dataSourceInitEvent.beforeCreate(dataSourceProperty);
        }
        DataSource dataSource = dataSourceCreator.createDataSource(dataSourceProperty);
        if (dataSourceInitEvent != null) {
            dataSourceInitEvent.afterCreate(dataSource);
        }
        this.runScrip(dataSource, dataSourceProperty);
        return wrapDataSource(dataSource, dataSourceProperty);
    }

    /**
     * 执行初始化脚本
     *
     * @param dataSource         数据源
     * @param dataSourceProperty 数据源参数
     */
    private void runScrip(DataSource dataSource, DataSourceProperty dataSourceProperty) {
        DatasourceInitProperties initProperty = dataSourceProperty.getInit();
        String schema = initProperty.getSchema();
        String data = initProperty.getData();
        if (DsStrUtils.hasText(schema) || DsStrUtils.hasText(data)) {
            ScriptRunner scriptRunner = new ScriptRunner(initProperty.isContinueOnError(), initProperty.getSeparator());
            if (DsStrUtils.hasText(schema)) {
                scriptRunner.runScript(dataSource, schema);
            }
            if (DsStrUtils.hasText(data)) {
                scriptRunner.runScript(dataSource, data);
            }
        }
    }

    /**
     * 包装数据源
     *
     * @param dataSource         数据源
     * @param dataSourceProperty 数据源参数
     * @return
     */
    private DataSource wrapDataSource(DataSource dataSource, DataSourceProperty dataSourceProperty) {
        String name = dataSourceProperty.getPoolName();
        DataSource targetDataSource = dataSource;

        Boolean enabledP6spy = p6spy && dataSourceProperty.getP6spy();
        if (enabledP6spy) {
            targetDataSource = new P6DataSource(dataSource);
            log.debug("dynamic-datasource [{}] wrap p6spy plugin", name);
        }

        Boolean enabledSeata = seata && dataSourceProperty.getSeata();
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