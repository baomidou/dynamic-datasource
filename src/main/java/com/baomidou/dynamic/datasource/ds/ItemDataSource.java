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
package com.baomidou.dynamic.datasource.ds;

import com.baomidou.dynamic.datasource.enums.SeataMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ItemDataSource extends AbstractDataSource implements Closeable {

    private String name;

    private DataSource realDataSource;

    private DataSource dataSource;

    private Boolean p6spy;

    private Boolean seata;

    private SeataMode seataMode;

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return dataSource.getConnection(username, password);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return super.isWrapperFor(iface) || iface.isInstance(realDataSource) || iface.isInstance(dataSource);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        if (iface.isInstance(realDataSource)) {
            return (T) realDataSource;
        }
        if (iface.isInstance(dataSource)) {
            return (T) dataSource;
        }
        return null;
    }

    @Override
    public void close() {
        Class<? extends DataSource> clazz = realDataSource.getClass();
        try {
            Method closeMethod = clazz.getDeclaredMethod("close");
            closeMethod.invoke(realDataSource);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.warn("dynamic-datasource close the datasource named [{}] failed,", name, e);
        }
    }
}
