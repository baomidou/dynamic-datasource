/**
 * Copyright © 2018 organization 苞米豆
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
package com.baomidou.dynamic.datasource.spring.boot.autoconfigure;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidDataSourceProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import javax.sql.DataSource;

/**
 * @author TaoYu
 * @since 1.2.0
 */
@Getter
@Setter
public class DynamicDataSource {

    /**
     * JDBC type,如果不设置自动查找 Druid > HikariCp
     */
    private Class<? extends DataSource> type;

    /**
     * JDBC driver
     */
    private String driverClassName;

    /**
     * JDBC url 地址
     */
    private String url;

    /**
     * JDBC 用户名
     */
    private String username;

    /**
     * JDBC 密码
     */
    private String password;

    /**
     * Druid参数配置
     */
    @NestedConfigurationProperty
    private DruidDataSourceProperties druid = new DruidDataSourceProperties();

    public DataSource initDataSource() {
        Class<?> builderClass = null;
        try {
            builderClass = Class.forName("org.springframework.boot.jdbc.DataSourceBuilder");
        } catch (Exception e) {
        }
        if (builderClass == null) {
            try {
                builderClass = Class.forName("org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder");
            } catch (Exception e) {
            }
        }
        try {
            Object o1 = builderClass.getDeclaredMethod("create").invoke(null);
            Object o2 = builderClass.getDeclaredMethod("type", Class.class).invoke(o1, this.type);
            Object o3 = builderClass.getDeclaredMethod("driverClassName", String.class).invoke(o2, this.driverClassName);
            Object o4 = builderClass.getDeclaredMethod("url", String.class).invoke(o3, this.url);
            Object o5 = builderClass.getDeclaredMethod("username", String.class).invoke(o4, this.username);
            Object o6 = builderClass.getDeclaredMethod("password", String.class).invoke(o5, this.password);
            return (DataSource) builderClass.getDeclaredMethod("build").invoke(o6);
        } catch (Exception e) {

        }
        return null;
    }

}
