/**
 * Copyright © 2018 organization 苞米豆
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
package com.baomidou.dynamic.datasource.util;

/**
 * 核心基于ThreadLocal的切换数据源工具类
 *
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
public final class DynamicDataSourceContextHolder {

    private static final ThreadLocal<String> LOOKUP_KEY_HOLDER = new ThreadLocal();

    private DynamicDataSourceContextHolder() {
    }

    public static String getDataSourceLookupKey() {
        return LOOKUP_KEY_HOLDER.get();
    }

    public static void setDataSourceLookupKey(String dataSourceLookupKey) {
        LOOKUP_KEY_HOLDER.set(dataSourceLookupKey);
    }

    public static void clearDataSourceLookupKey() {
        LOOKUP_KEY_HOLDER.remove();
    }

}
