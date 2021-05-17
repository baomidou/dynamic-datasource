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
package com.baomidou.dynamic.datasource.spring.boot.autoconfigure.dbcp2;

import lombok.Data;
import org.apache.commons.dbcp2.BasicDataSource;

import java.util.List;
import java.util.Set;

@Data
public class Dbcp2Config {

    private Boolean defaultAutoCommit;

    private Boolean defaultReadOnly;

    private Integer defaultTransactionIsolation;

    private Integer defaultQueryTimeoutSeconds;

    private String defaultCatalog;

    private String defaultSchema;

    private Boolean cacheState;

    private Boolean lifo;

    private Integer maxTotal;

    private Integer maxIdle;

    private Integer minIdle;

    private Integer initialSize;

    private Long maxWaitMillis;

    private Boolean poolPreparedStatements;

    private Boolean clearStatementPoolOnReturn;

    private Integer maxOpenPreparedStatements;

    private Boolean testOnCreate;

    private Boolean testOnBorrow;

    private Boolean testOnReturn;

    private Long timeBetweenEvictionRunsMillis;

    private Integer numTestsPerEvictionRun;

    private Long minEvictableIdleTimeMillis;

    private Long softMinEvictableIdleTimeMillis;

    private String evictionPolicyClassName;

    private Boolean testWhileIdle;

    private String validationQuery;

    private Integer validationQueryTimeoutSeconds;

    private String connectionFactoryClassName;

    private List<String> connectionInitSqls;

    private Boolean accessToUnderlyingConnectionAllowed;

    private Long maxConnLifetimeMillis;

    private Boolean logExpiredConnections;

    private String jmxName;

    private Boolean autoCommitOnReturn;

    private Boolean rollbackOnReturn;

    private Set<String> disconnectionSqlCodes;

    private Boolean fastFailValidation;

    private String connectionProperties;

    public BasicDataSource toDbcpDataSource(Dbcp2Config g) {
        BasicDataSource dataSource = new BasicDataSource();

        Boolean tempDefaultAutoCommit = defaultAutoCommit == null ? g.getDefaultAutoCommit() : defaultAutoCommit;
        if (tempDefaultAutoCommit != null) {
            dataSource.setDefaultAutoCommit(tempDefaultAutoCommit);
        }

        Boolean tempDefaultReadOnly = defaultReadOnly == null ? g.getDefaultReadOnly() : defaultReadOnly;
        if (tempDefaultReadOnly != null) {
            dataSource.setDefaultReadOnly(tempDefaultReadOnly);
        }

        Integer tempDefaultTransactionIsolation = defaultTransactionIsolation == null ? g.getDefaultTransactionIsolation() : defaultTransactionIsolation;
        if (tempDefaultTransactionIsolation != null) {
            dataSource.setDefaultTransactionIsolation(tempDefaultTransactionIsolation);
        }

        Integer tempDefaultQueryTimeoutSeconds = defaultQueryTimeoutSeconds == null ? g.getDefaultQueryTimeoutSeconds() : defaultQueryTimeoutSeconds;
        if (tempDefaultQueryTimeoutSeconds != null) {
            dataSource.setDefaultQueryTimeout(tempDefaultQueryTimeoutSeconds);
        }

        String tempDefaultCatalog = defaultCatalog == null ? g.getDefaultCatalog() : defaultCatalog;
        if (tempDefaultCatalog != null) {
            dataSource.setDefaultCatalog(tempDefaultCatalog);
        }

        String tempDefaultSchema = defaultSchema == null ? g.getDefaultSchema() : defaultSchema;
        if (tempDefaultSchema != null) {
            dataSource.setDefaultSchema(tempDefaultSchema);
        }

        Boolean tempCacheState = cacheState == null ? g.getCacheState() : cacheState;
        if (tempCacheState != null) {
            dataSource.setCacheState(tempCacheState);
        }

        Boolean tempLifo = lifo == null ? g.getLifo() : lifo;
        if (tempLifo != null) {
            dataSource.setLifo(tempLifo);
        }

        Integer tempMaxTotal = maxTotal == null ? g.getMaxTotal() : maxTotal;
        if (tempMaxTotal != null) {
            dataSource.setMaxTotal(tempMaxTotal);
        }

        Integer tempMaxIdle = maxIdle == null ? g.getMaxIdle() : maxIdle;
        if (tempMaxIdle != null) {
            dataSource.setMaxIdle(tempMaxIdle);
        }

        Integer tempMinIdle = minIdle == null ? g.getMinIdle() : minIdle;
        if (tempMinIdle != null) {
            dataSource.setMinIdle(tempMinIdle);
        }

        Integer tempInitialSize = initialSize == null ? g.getInitialSize() : initialSize;
        if (tempInitialSize != null) {
            dataSource.setInitialSize(tempInitialSize);
        }

        Long tempMaxWaitMillis = maxWaitMillis == null ? g.getMaxWaitMillis() : maxWaitMillis;
        if (tempMaxWaitMillis != null) {
            dataSource.setMaxWaitMillis(tempMaxWaitMillis);
        }

        Boolean tempPoolPreparedStatements = poolPreparedStatements == null ? g.getPoolPreparedStatements() : poolPreparedStatements;
        if (tempPoolPreparedStatements != null) {
            dataSource.setPoolPreparedStatements(tempPoolPreparedStatements);
        }

        Boolean tempClearStatementPoolOnReturn = clearStatementPoolOnReturn == null ? g.getClearStatementPoolOnReturn() : clearStatementPoolOnReturn;
        if (tempClearStatementPoolOnReturn != null) {
            dataSource.setClearStatementPoolOnReturn(tempClearStatementPoolOnReturn);
        }

        Integer tempMaxOpenPreparedStatements = maxOpenPreparedStatements == null ? g.getMaxOpenPreparedStatements() : maxOpenPreparedStatements;
        if (tempMaxOpenPreparedStatements != null) {
            dataSource.setMaxOpenPreparedStatements(tempMaxOpenPreparedStatements);
        }

        Boolean tempTestOnCreate = testOnCreate == null ? g.getTestOnCreate() : testOnCreate;
        if (tempTestOnCreate != null) {
            dataSource.setTestOnCreate(tempTestOnCreate);
        }

        Boolean tempTestOnBorrow = testOnBorrow == null ? g.getTestOnBorrow() : testOnBorrow;
        if (tempTestOnBorrow != null) {
            dataSource.setTestOnBorrow(tempTestOnBorrow);
        }

        Boolean tempTestOnReturn = testOnReturn == null ? g.getTestOnReturn() : testOnReturn;
        if (tempTestOnReturn != null) {
            dataSource.setTestOnReturn(tempTestOnReturn);
        }

        Long tempTimeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis == null ? g.getTimeBetweenEvictionRunsMillis() : timeBetweenEvictionRunsMillis;
        if (tempTimeBetweenEvictionRunsMillis != null) {
            dataSource.setTimeBetweenEvictionRunsMillis(tempTimeBetweenEvictionRunsMillis);
        }

        Integer tempNumTestsPerEvictionRun = numTestsPerEvictionRun == null ? g.getNumTestsPerEvictionRun() : numTestsPerEvictionRun;
        if (tempNumTestsPerEvictionRun != null) {
            dataSource.setNumTestsPerEvictionRun(tempNumTestsPerEvictionRun);
        }

        Long tempMinEvictableIdleTimeMillis = minEvictableIdleTimeMillis == null ? g.getMinEvictableIdleTimeMillis() : minEvictableIdleTimeMillis;
        if (tempMinEvictableIdleTimeMillis != null) {
            dataSource.setMinEvictableIdleTimeMillis(tempMinEvictableIdleTimeMillis);
        }

        Long tempSoftMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis == null ? g.getSoftMinEvictableIdleTimeMillis() : softMinEvictableIdleTimeMillis;
        if (tempSoftMinEvictableIdleTimeMillis != null) {
            dataSource.setSoftMinEvictableIdleTimeMillis(tempSoftMinEvictableIdleTimeMillis);
        }

        String tempEvictionPolicyClassName = evictionPolicyClassName == null ? g.getEvictionPolicyClassName() : evictionPolicyClassName;
        if (tempEvictionPolicyClassName != null) {
            dataSource.setEvictionPolicyClassName(tempEvictionPolicyClassName);
        }

        Boolean tempTestWhileIdle = testWhileIdle == null ? g.getTestWhileIdle() : testWhileIdle;
        if (tempTestWhileIdle != null) {
            dataSource.setTestWhileIdle(tempTestWhileIdle);
        }

        String tempValidationQuery = validationQuery == null ? g.getValidationQuery() : validationQuery;
        if (tempValidationQuery != null) {
            dataSource.setValidationQuery(tempValidationQuery);
        }

        Integer tempValidationQueryTimeoutSeconds = validationQueryTimeoutSeconds == null ? g.getValidationQueryTimeoutSeconds() : validationQueryTimeoutSeconds;
        if (tempValidationQueryTimeoutSeconds != null) {
            dataSource.setValidationQueryTimeout(tempValidationQueryTimeoutSeconds);
        }

        String tempConnectionFactoryClassName = connectionFactoryClassName == null ? g.getConnectionFactoryClassName() : connectionFactoryClassName;
        if (tempConnectionFactoryClassName != null) {
            dataSource.setConnectionFactoryClassName(tempConnectionFactoryClassName);
        }

        List<String> tempConnectionInitSqls = connectionInitSqls == null ? g.getConnectionInitSqls() : connectionInitSqls;
        if (tempConnectionInitSqls != null && tempConnectionInitSqls.size() > 0) {
            dataSource.setConnectionInitSqls(tempConnectionInitSqls);
        }

        Boolean tempAccessToUnderlyingConnectionAllowed = accessToUnderlyingConnectionAllowed == null ? g.getAccessToUnderlyingConnectionAllowed() : accessToUnderlyingConnectionAllowed;
        if (tempAccessToUnderlyingConnectionAllowed != null) {
            dataSource.setAccessToUnderlyingConnectionAllowed(tempAccessToUnderlyingConnectionAllowed);
        }

        Long tempMaxConnLifetimeMillis = maxConnLifetimeMillis == null ? g.getMaxConnLifetimeMillis() : maxConnLifetimeMillis;
        if (tempMaxConnLifetimeMillis != null) {
            dataSource.setMaxConnLifetimeMillis(tempMaxConnLifetimeMillis);
        }

        Boolean tempLogExpiredConnections = logExpiredConnections == null ? g.getLogExpiredConnections() : logExpiredConnections;
        if (tempLogExpiredConnections != null) {
            dataSource.setLogExpiredConnections(tempLogExpiredConnections);
        }

        String tempJmxName = jmxName == null ? g.getJmxName() : jmxName;
        if (tempJmxName != null) {
            dataSource.setJmxName(tempJmxName);
        }

        Boolean tempAutoCommitOnReturn = autoCommitOnReturn == null ? g.getAutoCommitOnReturn() : autoCommitOnReturn;
        if (tempAutoCommitOnReturn != null) {
            dataSource.setAutoCommitOnReturn(tempAutoCommitOnReturn);
        }

        Boolean tempRollbackOnReturn = rollbackOnReturn == null ? g.getRollbackOnReturn() : rollbackOnReturn;
        if (tempRollbackOnReturn != null) {
            dataSource.setRollbackOnReturn(tempRollbackOnReturn);
        }

        Set<String> tempDisconnectionSqlCodes = disconnectionSqlCodes == null ? g.getDisconnectionSqlCodes() : disconnectionSqlCodes;
        if (tempDisconnectionSqlCodes != null && tempDisconnectionSqlCodes.size() > 0) {
            dataSource.setDisconnectionSqlCodes(tempDisconnectionSqlCodes);
        }

        Boolean tempFastFailValidation = fastFailValidation == null ? g.getFastFailValidation() : fastFailValidation;
        if (tempFastFailValidation != null) {
            dataSource.setFastFailValidation(tempFastFailValidation);
        }

        String tempConnectionProperties = connectionProperties == null ? g.getConnectionProperties() : connectionProperties;
        if (tempConnectionProperties != null) {
            dataSource.setConnectionProperties(tempConnectionProperties);
        }
        return dataSource;
    }
}
