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

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author funkye
 */
public class ConnectionFactory {

    private static final ThreadLocal<Map<String, ConnectionProxy>> CONNECTION_HOLDER =
            new ThreadLocal<Map<String, ConnectionProxy>>() {
                @Override
                protected Map<String, ConnectionProxy> initialValue() {
                    return new ConcurrentHashMap<>();
                }
            };

    public static void putConnection(String dsKey, ConnectionProxy connection) {
        Map<String, ConnectionProxy> map = CONNECTION_HOLDER.get();
        if (!map.containsKey(dsKey)) {
            try {
                connection.setAutoCommit(false);
                map.put(dsKey, connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static ConnectionProxy getConnection(String ds) {
        return CONNECTION_HOLDER.get().get(ds);
    }

    public static void commit() {
        try {
            for (ConnectionProxy conn : CONNECTION_HOLDER.get().values()) {
                try {
                    conn.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            CONNECTION_HOLDER.remove();
        }
    }

    public static void rollback() {
        try {
            for (ConnectionProxy conn : CONNECTION_HOLDER.get().values()) {
                try {
                    conn.rollback();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            CONNECTION_HOLDER.remove();
        }
    }
}