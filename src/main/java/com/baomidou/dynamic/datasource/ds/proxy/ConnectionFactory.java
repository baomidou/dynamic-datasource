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
package com.baomidou.dynamic.datasource.ds.proxy;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author funkye
 */
public class ConnectionFactory {

    private static volatile ConcurrentHashMap<String, List<ConnectionProxy>> concurrentHashMap =
        new ConcurrentHashMap<>();

    private static ReentrantLock lock = new ReentrantLock();

    public static ConcurrentHashMap<String, List<ConnectionProxy>> getConcurrentHashMap() {
        return concurrentHashMap;
    }

    public static void setConcurrentHashMap(ConcurrentHashMap<String, List<ConnectionProxy>> concurrentHashMap) {
        ConnectionFactory.concurrentHashMap = concurrentHashMap;
    }

    public static void putConnection(String xid, ConnectionProxy connection) {
        lock.lock();
        try {
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
        } finally {
            lock.unlock();
        }
    }

    public static ConnectionProxy getConnection() {
        lock.lock();
        try {
            List<ConnectionProxy> list = concurrentHashMap.get(TransactionContext.getXID());
            if (!CollectionUtils.isEmpty(list)) {
                String ds = DynamicDataSourceContextHolder.peek();
                for (ConnectionProxy connectionProxy : list) {
                    if (connectionProxy.getDs().equals(ds)) {
                        return connectionProxy;
                    }
                }
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    public static void notify(String xid, Boolean state) {
        lock.lock();
        try {
            List<ConnectionProxy> list = concurrentHashMap.get(xid);
            if (!CollectionUtils.isEmpty(list)) {
                for (ConnectionProxy conn : list) {
                    conn.notify(state);
                }
                concurrentHashMap.remove(xid);
            }
        } finally {
            lock.unlock();
        }
    }

    public static void remove(String xid) {
        concurrentHashMap.remove(xid);
    }
}