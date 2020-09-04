package com.baomidou.dynamic.datasource.ds.proxy;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 陈健斌
 */
public class ConnectionFactory {

    private static volatile ConcurrentHashMap<String, List<ConnectionProxy>> concurrentHashMap =
        new ConcurrentHashMap<>();

    private static ReentrantLock lock = new ReentrantLock();

    public static ConcurrentHashMap<String, List<ConnectionProxy>> getConcurrentHashMap() {
        return concurrentHashMap;
    }

    public static void setConcurrentHashMap(ConcurrentHashMap<String, List<ConnectionProxy>> concurrentHashMap) {
        ConnectionFactory.concurrentHashMap = concurrentHashMap;
    }

    public static void putConnection(String xid, ConnectionProxy connection) {
        lock.lock();
        try {
            List<ConnectionProxy> list = concurrentHashMap.get(xid);
            if (CollectionUtils.isEmpty(list)) {
                list = new ArrayList<>();
                concurrentHashMap.put(xid, list);
            }
            list.add(connection);
        } finally {
            lock.unlock();
        }
    }

    public static List<ConnectionProxy> notify(String xid, Boolean state) {
        lock.lock();
        try {
            List<ConnectionProxy> list = concurrentHashMap.get(xid);
            if (!CollectionUtils.isEmpty(list)) {
                list.forEach(conn -> {
                    conn.notify(state);
                });
            }
        } finally {
            lock.unlock();
        }
        return concurrentHashMap.get(xid);
    }

    public static void remove(String xid) {
        concurrentHashMap.remove(xid);
    }
}