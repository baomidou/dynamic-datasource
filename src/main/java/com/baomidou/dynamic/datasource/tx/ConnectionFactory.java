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
import java.util.ArrayList;
import java.util.List;

/**
 * @author funkye
 */
public class ConnectionFactory {

    private static final ThreadLocal<List<ConnectionProxy>> CONNECTION_HOLDER =
            new ThreadLocal<List<ConnectionProxy>>() {
                @Override
                protected List<ConnectionProxy> initialValue() {
                    return new ArrayList<>();
                }
            };

    public static void putConnection(ConnectionProxy connection) {
        List<ConnectionProxy> list = CONNECTION_HOLDER.get();
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        list.add(connection);
    }

    public static ConnectionProxy getConnection(String ds) {
        List<ConnectionProxy> list = CONNECTION_HOLDER.get();
        for (ConnectionProxy connectionProxy : list) {
            if (connectionProxy.getDs().equals(ds)) {
                return connectionProxy;
            }
        }

        return null;
    }

    public static void notify(Boolean state) {
        try {
            List<ConnectionProxy> list = CONNECTION_HOLDER.get();
            for (ConnectionProxy conn : list) {
                conn.notify(state);
            }
        } finally {
            CONNECTION_HOLDER.remove();
        }
    }

}