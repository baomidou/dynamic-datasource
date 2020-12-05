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
package com.baomidou.dynamic.datasource.tx;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author funkye
 */
public class ConnectionFactory {

    private static final ThreadLocal<Map<String, List<ConnectionProxy>>> CONNECTION_HOLDER =
        new ThreadLocal<Map<String, List<ConnectionProxy>>>() {
            @Override
            protected Map<String, List<ConnectionProxy>> initialValue() {
                return new ConcurrentHashMap<>();
            }
        };

    public static void putConnection(String xid, ConnectionProxy connection) {
        synchronized (xid) {
            Map<String, List<ConnectionProxy>> concurrentHashMap = CONNECTION_HOLDER.get();
            List<ConnectionProxy> list = concurrentHashMap.get(xid);
            if (CollectionUtils.isEmpty(list)) {
                list = new ArrayList<>();
                concurrentHashMap.put(xid, list);
            }
            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            list.add(connection);
        }

    }

    public static ConnectionProxy getConnection() {
        String xid = TransactionContext.getXID();
        synchronized (xid) {
            Map<String, List<ConnectionProxy>> concurrentHashMap = CONNECTION_HOLDER.get();
            List<ConnectionProxy> list = concurrentHashMap.get(xid);
            if (!CollectionUtils.isEmpty(list)) {
                String ds = DynamicDataSourceContextHolder.peek();
                for (ConnectionProxy connectionProxy : list) {
                    if (connectionProxy.getDs().equals(ds)) {
                        return connectionProxy;
                    }
                }
            }
        }
        return null;
    }

    public static void notify(String xid, Boolean state) {
        synchronized (xid) {
            try {
                Map<String, List<ConnectionProxy>> concurrentHashMap = CONNECTION_HOLDER.get();
                List<ConnectionProxy> list = concurrentHashMap.get(xid);
                if (!CollectionUtils.isEmpty(list)) {
                    for (ConnectionProxy conn : list) {
                        conn.notify(state);
                    }
                    concurrentHashMap.remove(xid);
                }
            } finally {
                CONNECTION_HOLDER.remove();
            }
        }
    }

}