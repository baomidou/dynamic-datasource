package com.baomidou.dynamic.datasource.ds.proxy;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import java.util.HashMap;
import java.util.Map;

public class TransactionContext {

    private static final ThreadLocal<Map<String, String>> CONTEXT_HOLDER =
        ThreadLocal.withInitial(() -> new HashMap<>());

    private static final String XID = "LOCAL_XID";

    /**
     * Gets xid.
     *
     * @return the xid
     */
    public static String getXID() {
        String xid = CONTEXT_HOLDER.get().get(XID);
        if (StringUtils.isNotBlank(xid)) {
            return xid;
        }
        return null;
    }

    /**
     * Unbind string.
     *
     * @return the string
     */
    public static String unbind(String xid) {
        CONTEXT_HOLDER.get().remove(xid);
        return xid;
    }

    /**
     * bind string.
     *
     * @return the string
     */
    public static String bind(String xid) {
        CONTEXT_HOLDER.get().put(XID, xid);
        return xid;
    }

    /**
     * remove
     */
    public static void remove() {
        CONTEXT_HOLDER.remove();
    }

}
