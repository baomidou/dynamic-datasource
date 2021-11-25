package com.baomidou.dynamic.datasource.tx;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * 本地事务工具类
 *
 * @author TaoYu
 * @since 3.5.0
 */
@Slf4j
public final class LocalTxUtil {

    /**
     * 手动开启事务
     */
    public static void startTransaction() {
        if (!StringUtils.isEmpty(TransactionContext.getXID())) {
            log.debug("dynamic-datasource exist local tx {}", TransactionContext.getXID());
        } else {
            String xid = UUID.randomUUID().toString();
            TransactionContext.bind(xid);
            log.debug("dynamic-datasource start local tx {}" + xid);
        }
    }

    /**
     * 手动提交事务
     */
    public static void commit() {
        ConnectionFactory.notify(true);
        log.debug("dynamic-datasource commit local tx {}" + TransactionContext.getXID());
        TransactionContext.remove();
    }

    /**
     * 手动回滚事务
     */
    public static void rollback() {
        ConnectionFactory.notify(false);
        log.info("dynamic-datasource rollback local tx {}" + TransactionContext.getXID());
        TransactionContext.remove();
    }
}
