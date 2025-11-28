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
package com.baomidou.dynamic.datasource.creator.c3p0;

import lombok.Getter;
import lombok.Setter;


/**
 * @author Joy
 */
@Getter
@Setter
public class C3p0Config {
    private Integer acquireIncrement;
    private Integer acquireRetryAttempts;
    private Integer acquireRetryDelay;
    private Boolean attemptResurrectOnCheckin;
    private Boolean autoCommitOnClose;
    private String automaticTestTable;
    private Boolean breakAfterAcquireFailure;
    private Integer checkoutTimeout;
    private String connectionCustomizerClassName;
    private Integer connectionIsValidTimeout;
    private String connectionTesterClassName;
    private String contextClassLoaderSource;
    private Boolean debugUnreturnedConnectionStackTraces;
    private String factoryClassLocation;
    private Boolean forceIgnoreUnresolvedTransactions;
    private Boolean forceSynchronousCheckins;
    private Integer idleConnectionTestPeriod;
    private Integer initialPoolSize;
    private String markSessionBoundaries;
    private Integer maxAdministrativeTaskTime;
    private Integer maxConnectionAge;
    private Integer maxIdleTime;
    private Integer maxIdleTimeExcessConnections;
    private Integer maxPoolSize;
    private Integer maxStatements;
    private Integer maxStatementsPerConnection;
    private Integer minPoolSize;
    private String overrideDefaultPassword;
    private String overrideDefaultUser;
    private String preferredTestQuery;
    private Boolean privilegeSpawnedThreads;
    private Integer propertyCycle;
    private Integer statementCacheNumDeferredCloseThreads;
    private String taskRunnerFactoryClassName;
    private Boolean testConnectionOnCheckin;
    private Boolean testConnectionOnCheckout;
    private Integer unreturnedConnectionTimeout;
}
