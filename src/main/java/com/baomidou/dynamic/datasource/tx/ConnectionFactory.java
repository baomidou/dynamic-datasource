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
package com.baomidou.dynamic.datasource.tx;

import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author funkye
 */
public class ConnectionFactory {

    private static final ThreadLocal<Map<String, Map<String, ConnectionProxy>>> CONNECTION_HOLDER =
            new ThreadLocal<Map<String, Map<String, ConnectionProxy>>>() {
                @Override
                protected Map<String, Map<String, ConnectionProxy>> initialValue() {
                    return new ConcurrentHashMap<>();
                }
            };

    public static void putConnection(String xid, String ds, ConnectionProxy connection) {
        Map<String, Map<String, ConnectionProxy>> concurrentHashMap = CONNECTION_HOLDER.get();
        Map<String, ConnectionProxy> connectionProxyMap = concurrentHashMap.get(xid);
        if (connectionProxyMap == null) {
            connectionProxyMap = new ConcurrentHashMap<>();
            concurrentHashMap.put(xid, connectionProxyMap);
        }
        if (!connectionProxyMap.containsKey(ds)) {
            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            connectionProxyMap.put(ds, connection);
        }
    }

    public static ConnectionProxy getConnection(String xid, String ds) {
        Map<String, Map<String, ConnectionProxy>> concurrentHashMap = CONNECTION_HOLDER.get();
        Map<String, ConnectionProxy> connectionProxyMap = concurrentHashMap.get(xid);
        if (CollectionUtils.isEmpty(connectionProxyMap)) {
            return null;
        }
        return connectionProxyMap.get(ds);
    }

    public static void notify(String xid, Boolean state) throws Exception {
        Exception exception = null;
        Map<String, Map<String, ConnectionProxy>> concurrentHashMap = CONNECTION_HOLDER.get();
        try {
            if (CollectionUtils.isEmpty(concurrentHashMap)) {
                return;
            }
            Map<String, ConnectionProxy> connectionProxyMap = concurrentHashMap.get(xid);
            for (ConnectionProxy connectionProxy : connectionProxyMap.values()) {
                try {
                    if (connectionProxy != null) {
                        connectionProxy.notify(state);
                    }
                } catch (SQLException e) {
                    exception = e;
                }

            }
        } finally {
            concurrentHashMap.remove(xid);
            if (exception != null) {
                throw exception;
            }
        }
    }

}
