/**
 * Copyright © 2018 organization baomidou
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
package com.baomidou.dynamic.datasource.toolkit;

import java.util.LinkedList;
import java.util.List;

/**
 * 核心基于ThreadLocal的切换数据源工具类
 *
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
public final class DynamicDataSourceContextHolder {

    @SuppressWarnings("unchecked")
    private static final ThreadLocal<List<String>> LOOKUP_KEY_HOLDER = new ThreadLocal();

    private DynamicDataSourceContextHolder() {
    }

    /**
     * 获得当前线程数据源
     *
     * @return 数据源名称
     */
    public static String getDataSourceLookupKey() {
        List<String> lookupKeys = LOOKUP_KEY_HOLDER.get();
        if (lookupKeys == null || lookupKeys.isEmpty()) {
            return null;
        }
        return lookupKeys.get(0);
    }

    /**
     * 设置当前线程数据源
     */
    public static void setDataSourceLookupKey(String dataSourceLookupKey) {
        List<String> lookupKeys = LOOKUP_KEY_HOLDER.get();
        if (lookupKeys == null) {
            lookupKeys = new LinkedList<>();
        }
        lookupKeys.add(dataSourceLookupKey);
        LOOKUP_KEY_HOLDER.set(lookupKeys);
    }

    /**
     * 清空当前线程数据源
     * <p>
     * 如果当前线程是连续切换数据源
     * 只会移除掉当前线程的数据源名称
     * </p>
     */
    public static void clearDataSourceLookupKey() {
        List<String> lookupKeys = LOOKUP_KEY_HOLDER.get();
        if (lookupKeys.isEmpty()) {
            LOOKUP_KEY_HOLDER.remove();
        } else {
            lookupKeys.remove(0);
        }
    }
}
