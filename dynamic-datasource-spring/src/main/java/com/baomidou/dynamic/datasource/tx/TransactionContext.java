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

import com.baomidou.dynamic.datasource.toolkit.DsStrUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.transaction.support.TransactionSynchronization;

import java.util.*;

/**
 * @author funkye zp
 */
public class TransactionContext {

    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<Set<TransactionSynchronization>> SYNCHRONIZATION_HOLDER =
            ThreadLocal.withInitial(LinkedHashSet::new);

    /**
     * Gets xid.
     *
     * @return 事务ID
     */
    public static String getXID() {
        String xid = CONTEXT_HOLDER.get();
        if (!DsStrUtils.isEmpty(xid)) {
            return xid;
        }
        return null;
    }

    /**
     * Unbind string.
     *
     * @param xid 事务ID
     * @return the string
     */
    public static String unbind(String xid) {
        CONTEXT_HOLDER.remove();
        return xid;
    }

    /**
     * bind xid.
     *
     * @param xid 事务ID
     * @return the string
     */
    public static String bind(String xid) {
        CONTEXT_HOLDER.set(xid);
        return xid;
    }

    /**
     * remove
     */
    public static void remove() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * Register synchronization.
     *
     * @param synchronization 事务同步信息
     */
    public static void registerSynchronization(TransactionSynchronization synchronization) {
        if (Objects.isNull(synchronization)) {
            throw new IllegalArgumentException("TransactionSynchronization must not be null");
        }
        if (DsStrUtils.isEmpty(TransactionContext.getXID())) {
            throw new IllegalStateException("Transaction is not active");
        }
        Set<TransactionSynchronization> synchs = SYNCHRONIZATION_HOLDER.get();
        synchs.add(synchronization);
    }

    /**
     * Get synchronization list.
     *
     * @return List<TransactionSynchronization> synchronizations
     */
    public static List<TransactionSynchronization> getSynchronizations() {
        Set<TransactionSynchronization> synchs = SYNCHRONIZATION_HOLDER.get();
        //to avoid ConcurrentModificationExceptions.
        if (synchs.isEmpty()) {
            return Collections.emptyList();
        } else {
            // Sort lazily here, not in registerSynchronization.
            List<TransactionSynchronization> sortedSynchs = new ArrayList<>(synchs);
            AnnotationAwareOrderComparator.sort(sortedSynchs);
            return Collections.unmodifiableList(sortedSynchs);
        }
    }

    /**
     * Remove synchronizations.
     */
    public static void removeSynchronizations() {
        SYNCHRONIZATION_HOLDER.remove();
    }

}