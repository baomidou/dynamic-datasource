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
            log.debug("dynamic-datasource exist local tx [{}]", TransactionContext.getXID());
        } else {
            String xid = UUID.randomUUID().toString();
            TransactionContext.bind(xid);
            log.debug("dynamic-datasource start local tx [{}]", xid);
        }
    }

    /**
     * 手动提交事务
     */
    public static void commit() {
        ConnectionFactory.notify(true);
        log.debug("dynamic-datasource commit local tx [{}]", TransactionContext.getXID());
        TransactionContext.remove();
    }

    /**
     * 手动回滚事务
     */
    public static void rollback() {
        ConnectionFactory.notify(false);
        log.debug("dynamic-datasource rollback local tx [{}]", TransactionContext.getXID());
        TransactionContext.remove();
    }
}
