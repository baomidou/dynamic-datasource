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
