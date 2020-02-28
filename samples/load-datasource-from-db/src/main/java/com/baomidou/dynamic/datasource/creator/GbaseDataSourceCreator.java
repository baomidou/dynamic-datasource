/**
 * Copyright © 2020 organization humingfeng
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
package cn.humingfeng.dynamic.datasource.creator;

import cn.humingfeng.dynamic.datasource.exception.ErrorCreateDataSourceException;
import cn.humingfeng.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.lang.reflect.Method;

/**
 * 基础Gbase数据源创建器
 *
 * @author HuMingfeng
 * @since 2020/2/28
 */
@Data
@Slf4j
public class GbaseDataSourceCreator {

    private static final Log LOG = LogFactory.getLog(GbaseDataSourceCreator.class);

    private static Method createMethod;
    private static Method typeMethod;
    private static Method urlMethod;
    private static Method usernameMethod;
    private static Method passwordMethod;
    private static Method driverClassNameMethod;
    private static Method buildMethod;

    static {
        //to support springboot 1.5 and 2.x
        Class<?> builderClass = null;
        try {
            builderClass = Class.forName("org.springframework.boot.jdbc.DataSourceBuilder");
        } catch (Exception ignored) {
        }
        if (builderClass == null) {
            try {
                builderClass = Class.forName("org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder");
            } catch (Exception e) {
                log.warn("not in springBoot ENV,could not create BasicDataSourceCreator");
            }
        }
        if (builderClass != null) {
            try {
                createMethod = builderClass.getDeclaredMethod("create");
                typeMethod = builderClass.getDeclaredMethod("type", Class.class);
                urlMethod = builderClass.getDeclaredMethod("url", String.class);
                usernameMethod = builderClass.getDeclaredMethod("username", String.class);
                passwordMethod = builderClass.getDeclaredMethod("password", String.class);
                driverClassNameMethod = builderClass.getDeclaredMethod("driverClassName", String.class);
                buildMethod = builderClass.getDeclaredMethod("build");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建基础数据源，暂时用org.apache.commons.dbcp2，生产环境验证
     *
     * @param dataSourceProperty 数据源参数
     * @return 数据源
     */
    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        try {
            Object o1 = createMethod.invoke(null);
            Object o2 = typeMethod.invoke(o1, BasicDataSource.class);
            Object o3 = urlMethod.invoke(o2, dataSourceProperty.getUrl());
            Object o4 = usernameMethod.invoke(o3, dataSourceProperty.getUsername());
            Object o5 = passwordMethod.invoke(o4, dataSourceProperty.getPassword());
            Object o6 = driverClassNameMethod.invoke(o5, dataSourceProperty.getDriverClassName());
            DataSource dataSource = (DataSource) buildMethod.invoke(o6);
            LOG.info("dynamic-datasource create gbase database named " + dataSourceProperty.getPollName() + " succeed");
            return dataSource;
        } catch (Exception e) {
            LOG.error("dynamic-datasource create basic database named " + dataSourceProperty.getPollName() + " error");
            throw new ErrorCreateDataSourceException(
                    "dynamic-datasource create basic database named " + dataSourceProperty.getPollName() + " error");
        }
    }
}
