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
package com.baomidou.dynamic.datasource.creator.oracleucp;

import lombok.Getter;
import lombok.Setter;

import java.util.Properties;

/**
 * OracleUCP参数配置
 *
 * @author Shaoyu Liu
 */
@Getter
@Setter
public class OracleUcpConfig {

    private Integer initialPoolSize;
    private Integer minPoolSize;
    private Integer maxPoolSize;
    private Integer maxIdleTime;
    private Boolean fastConnectionFailoverEnabled;
    private Integer maxConnectionReuseCount;
    private Long maxConnectionReuseTime;
    private Integer inactiveConnectionTimeout;
    private Integer maxStatements;
    private Integer abandonedConnectionTimeout;
    private Integer connectionHarvestMaxCount;
    private Integer connectionHarvestTriggerCount;
    private Integer connectionWaitTimeout;
    private Boolean validateConnectionOnBorrow;
    private String sqlForValidateConnection;
    private Integer timeoutCheckInterval;
    private Integer propertyCycle;
    private Integer timeToLiveConnectionTimeout;
    private Integer highCostConnectionReuseThreshold;
    private Integer connectionLabelingHighCost;
    private String onsConfiguration;
    private Integer loginTimeout;
    private Integer secondsToTrustIdleConnection;
    private Integer connectionRepurposeThreshold;
    private String connectionFactoryClassName;
    private Properties connectionFactoryProperty;
    private Properties connectionProperty;
    private Integer maxConnectionsPerService;
    private Integer maxConnectionsPerShard;

    private String connectionLabelingCallbackClassName;
    private String connectionAffinityCallbackClassName;
    private String connectionInitializationCallbackClassName;

}
