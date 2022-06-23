package com.baomidou.dynamic.datasource.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.core.NamedThreadLocal;
import org.springframework.core.OrderComparator;
import org.springframework.util.Assert;

public abstract class DsTransactionSynchronizationManager {
    private static final ThreadLocal<Set<DsTransactionSynchronization>> synchronizations = new NamedThreadLocal("DS Transaction synchronizations");


    public static boolean isSynchronizationActive() {
        return synchronizations.get() != null;
    }


    public static void initSynchronization() throws IllegalStateException {
        if (isSynchronizationActive()) {
            throw new IllegalStateException("Cannot activate ds  transaction synchronization - already active");
        } else {
            synchronizations.set(new LinkedHashSet<DsTransactionSynchronization>());
        }
    }

    public static void registerSynchronization(DsTransactionSynchronization synchronization) throws IllegalStateException {
        Assert.notNull(synchronization, "DS TransactionSynchronization must not be null");
        Set<DsTransactionSynchronization> synchs = synchronizations.get();
        if (synchs == null) {
            throw new IllegalStateException("DS Transaction synchronization is not active");
        } else {
            synchs.add(synchronization);
        }
    }

    public static List<DsTransactionSynchronization> getSynchronizations() throws IllegalStateException {
        Set<DsTransactionSynchronization> synchs = synchronizations.get();
        if (synchs == null) {
            throw new IllegalStateException("DS Transaction synchronization is not active");
        } else if (synchs.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<DsTransactionSynchronization> sortedSynchs = new ArrayList<>(synchs);
            OrderComparator.sort(sortedSynchs);
            return Collections.unmodifiableList(sortedSynchs);
        }
    }

    public static void clearSynchronization() throws IllegalStateException {
        if (!isSynchronizationActive()) {
            throw new IllegalStateException("Cannot deactivate ds transaction synchronization - not active");
        } else {
            synchronizations.remove();
        }
    }

    public static void clear() {
        synchronizations.remove();
    }
}
