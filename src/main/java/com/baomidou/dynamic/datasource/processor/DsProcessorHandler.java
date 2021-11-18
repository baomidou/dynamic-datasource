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
package com.baomidou.dynamic.datasource.processor;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.dynamic.datasource.exception.CannotFindDataSourceException;
import com.baomidou.dynamic.datasource.exception.CannotFoundDsProcessorException;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

/**
 * ds processor 容器
 * 
 * 将所有可用的processor统一管理
 * 
 * 拓展时可通过继承{@link DsProcessor}来实现不同的processor处理
 * 
 * 注意：拓展时继承{@link DsProcessor}的bean需要由spring容器管理
 *
 * @author nukiyoam
 */
@Slf4j
public class DsProcessorHandler {

    /**
     * 统一管理所有的processor
     */
    @Autowired
    private List<DsProcessor> processors = new ArrayList<>();

    /**
     * add processor to list
     * 
     * @param processor
     *            processor
     */
    public void addProcessor(DsProcessor processor) {
        if (processor == null) {
            throw new IllegalArgumentException("add processor cant not be null");
        }
        this.processors.add(processor);
    }

    /**
     * add processor to list
     * 
     * @param processors
     *            one or more processor
     */
    public void addProcessor(DsProcessor... processors) {
        if (processors == null || processors.length == 0) {
            throw new IllegalArgumentException("add processor cant not be null or empty array");
        }
        for (DsProcessor dsProcessor : processors) {
            this.addProcessor(dsProcessor);
        }
    }

    /**
     * 决定数据源
     * 
     * 将所有的processor挨个匹配，找到能够处理的processor
     * 
     * 如果所有processor都匹配完，还是没有找到，则抛出{@link CannotFoundDsProcessorException}异常
     *
     * @param invocation
     *            方法执行信息
     * @param key
     *            DS注解里的内容
     * @return 数据源名称
     */
    public String determineDatasource(MethodInvocation invocation, String key) {
        if (CollectionUtils.isEmpty(this.processors)) {
            throw new CannotFoundDsProcessorException("cant not found any ds processor");
        }
        String datasource = null;
        boolean matchedProcessor = false;
        for (DsProcessor dsProcessor : this.processors) {
            if (dsProcessor.matches(key)) {
                log.info("mathed processor [{}] for key [{}]", dsProcessor.getClass(), key);
                matchedProcessor = true;
                datasource = dsProcessor.doDetermineDatasource(invocation, key);
                if (datasource != null) {
                    break;
                } else {
                    return dsProcessor.doDetermineDatasource(invocation, key);
                }
            }
        }
        if (!matchedProcessor) {
            throw new CannotFoundDsProcessorException("cant not found any ds processor for key: " + key);
        }
        if (datasource == null || datasource.length() == 0) {
            throw new CannotFindDataSourceException("the datasource is null or empty for key: " + key);
        }
        return datasource;
    }

}
