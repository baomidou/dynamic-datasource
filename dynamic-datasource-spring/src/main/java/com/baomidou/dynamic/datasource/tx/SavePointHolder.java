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


import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.sql.Savepoint;
import java.util.LinkedList;
import java.util.List;

/**
 * @author zp
 */
public class SavePointHolder {
    private static final String SAVEPOINT_NAME_PREFIX = "DYNAMIC_";
    private ConnectionProxy connectionProxy;
    private LinkedList<Savepoint> savepoints;

    public SavePointHolder(ConnectionProxy connectionProxy) {
        this.connectionProxy = connectionProxy;
        this.savepoints = new LinkedList<>();
    }

    public void conversionSavePointHolder() throws SQLException {
        if (connectionProxy == null) {
            throw new SQLTransientConnectionException();
        }
        int savepointCounter = connectionProxy.getSavepointCounter();
        Savepoint savepoint = connectionProxy.setSavepoint(SAVEPOINT_NAME_PREFIX + savepointCounter);
        connectionProxy.setSavepointCounter(savepointCounter + 1);
        savepoints.addLast(savepoint);
    }

    public int releaseSavepoint() throws SQLException {
        Savepoint savepoint = savepoints.pollLast();
        if (savepoint != null) {
            connectionProxy.releaseSavepoint(savepoint);
            String savepointName = savepoint.getSavepointName();
            return Integer.parseInt(savepointName.substring(SAVEPOINT_NAME_PREFIX.length()));
        }
        return -1;
    }

    public int rollbackSavePoint() throws SQLException {
        Savepoint savepoint = savepoints.pollLast();
        if (savepoint != null) {
            connectionProxy.rollback(savepoint);
            String savepointName = savepoint.getSavepointName();
            return Integer.parseInt(savepointName.substring(SAVEPOINT_NAME_PREFIX.length()));
        }
        return -1;
    }

    public ConnectionProxy getConnectionProxy() {
        return this.connectionProxy;
    }

    public List<Savepoint> getSavePoints() {
        return this.savepoints;
    }
}