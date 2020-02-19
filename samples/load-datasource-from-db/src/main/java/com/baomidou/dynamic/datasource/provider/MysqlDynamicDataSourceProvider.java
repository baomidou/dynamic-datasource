/**
 * Copyright © 2019 organization humingfeng
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
package com.baomidou.dynamic.datasource.provider;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.toolkit.CryptoUtils;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.StringUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 其他数据源创建器
 *
 * @author humingfeng
 * @since 2.3.0
 */
public class MysqlDynamicDataSourceProvider {

    private static final Logger log = LoggerFactory.getLogger(MysqlDynamicDataSourceProvider.class);

    /**
     * DRUID数据源类
     */
    private static final String DRUID_DATASOURCE = "com.alibaba.druid.pool.DruidDataSource";
    /**
     * HikariCp数据源
     */
    private static final String HIKARI_DATASOURCE = "com.zaxxer.hikari.HikariDataSource";
    /**
     * 国产Gbase数据源
     */
    private static final String GBASE_DATASOURCE = "com.gbase.jdbc.jdbc2.optional.GBaseDataSource";

    /**
     * 数据源表建表语句
     */
    private static String CREATE_TABLE_IF_NOT_EXISTS;

    static {
        CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS `data_source_config` (\n" +
                "  ID int(11) NOT NULL AUTO_INCREMENT,\n" +
                "  USER_NAME varchar(255) DEFAULT NULL COMMENT '用户名',\n" +
                "  PASSWORD varchar(255) DEFAULT NULL COMMENT '密码',\n" +
                "  URL varchar(255) DEFAULT NULL COMMENT '地址',\n" +
                "  DRIVER_CLASS_NAME varchar(500) DEFAULT NULL COMMENT '驱动',\n" +
                "  DB_NAME varchar(255) DEFAULT NULL COMMENT '数据源名称',\n" +
                "  TYPE varchar(255) DEFAULT NULL COMMENT '数据源类型',\n" +
                "  PRIMARY KEY (ID)\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;";
    }

    public Map<String, DataSourceProperty> run(DataSource dataSource) {

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            //如果表不存在则建立
            jdbcTemplate.execute(CREATE_TABLE_IF_NOT_EXISTS);

            List<Map<String, Object>> maps = jdbcTemplate.queryForList("SELECT TYPE,DB_NAME,USER_NAME,PASSWORD,URL,DRIVER_CLASS_NAME FROM DATA_SOURCE_CONFIG");

            Map<String, DataSourceProperty> dataSourcePropertiesMap = new HashMap<>(3);

            for(Map<String, Object> map : maps) {

                String type = map.get("TYPE")==null?"":map.get("TYPE").toString();
                String db_name = map.get("DB_NAME")==null?"":map.get("DB_NAME").toString();
                String user_name = map.get("USER_NAME")==null?"":map.get("USER_NAME").toString();
                String password = map.get("PASSWORD")==null?"":map.get("PASSWORD").toString();
                String url = map.get("URL")==null?"":map.get("URL").toString();
                String driver_class_name = map.get("DRIVER_CLASS_NAME")==null?"":map.get("DRIVER_CLASS_NAME").toString();

                if (StringUtils.isEmpty(db_name)
                        || StringUtils.isEmpty(user_name)
                        || StringUtils.isEmpty(password)
                        || StringUtils.isEmpty(url)
                        || StringUtils.isEmpty(driver_class_name)) {
                    log.error("param contain wrong in DATA_SOURCE_CONFIG  ");
                    return null;
                }

                DataSourceProperty dataSourceProperty = new DataSourceProperty();
                dataSourceProperty.setUrl(url);
                dataSourceProperty.setUsername(user_name);
                dataSourceProperty.setPassword(CryptoUtils.decrypt(password));
                dataSourceProperty.setDriverClassName(driver_class_name);
                switch (type) {
                    case DRUID_DATASOURCE:
                        dataSourceProperty.setType(DruidDataSource.class);
                        break;
                    case HIKARI_DATASOURCE:
                        dataSourceProperty.setType(HikariDataSource.class);
                        break;
                    default:
                        break;
                }
                dataSourcePropertiesMap.put(db_name,dataSourceProperty);
            }
            log.debug("others in DATA_SOURCE_CONFIG:"+dataSourcePropertiesMap);
            return dataSourcePropertiesMap;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
