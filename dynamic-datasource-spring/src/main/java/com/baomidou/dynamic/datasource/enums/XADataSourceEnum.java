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
package com.baomidou.dynamic.datasource.enums;

import com.baomidou.mybatisplus.annotation.DbType;
import lombok.Getter;

/**
 * 目前支持的XA数据源
 *
 * @author <a href="mailto:312290710@qq.com">jiazhifeng</a>
 * @date 2023/03/02 23:05
 */
@Getter
public enum XADataSourceEnum {
    ORACLE(DbType.ORACLE,"oracle.jdbc.xa.client.OracleXADataSource"),
    MYSQL(DbType.MYSQL, "com.mysql.cj.jdbc.MysqlXADataSource"),
    POSTGRE_SQL(DbType.POSTGRE_SQL, "org.postgresql.xa.PGXADataSource"),
    H2(DbType.H2, "org.h2.jdbcx.JdbcDataSource"),
    ;

    private final DbType dbType;
    private final String xaDataSourceClassName;

    XADataSourceEnum(DbType dbType, String xaDataSourceClassName) {
        this.dbType = dbType;
        this.xaDataSourceClassName = xaDataSourceClassName;
    }

    public static boolean contains(DbType dbType){
        for (XADataSourceEnum item : values()) {
            if (item.getDbType() == dbType) {
                return true;
            }
        }
        return false;
    }

    public static String getByDbType(DbType dbType){
        for (XADataSourceEnum item : values()) {
            if (item.getDbType() == dbType) {
                return item.getXaDataSourceClassName();
            }
        }
        return null;
    }
}
