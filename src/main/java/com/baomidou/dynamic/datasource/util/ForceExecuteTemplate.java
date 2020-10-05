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
package com.baomidou.dynamic.datasource.util;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author: luhui
 * @email: luhui60@jd.com
 * @date 2020/10/5 5:42 下午
 * @description:
 **/
public class ForceExecuteTemplate<T> {

    /**
     * Force execute.
     *
     * @param targets targets to be executed
     * @param callback force execute callback
     * @throws SQLException throw SQL exception after all targets are executed
     */
    public void execute(final Collection<T> targets, final ForceExecuteCallback<T> callback) throws SQLException {
        Collection<SQLException> exceptions = new LinkedList<>();
        for (T each : targets) {
            try {
                callback.execute(each);
            } catch (final SQLException ex) {
                exceptions.add(ex);
            }
        }
        throwSQLExceptionIfNecessary(exceptions);
    }

    private void throwSQLExceptionIfNecessary(final Collection<SQLException> exceptions) throws SQLException {
        if (exceptions.isEmpty()) {
            return;
        }
        SQLException ex = new SQLException();
        for (SQLException each : exceptions) {
            ex.setNextException(each);
        }
        throw ex;
    }

}
