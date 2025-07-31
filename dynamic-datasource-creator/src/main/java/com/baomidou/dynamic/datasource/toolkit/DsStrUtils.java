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
package com.baomidou.dynamic.datasource.toolkit;

/**
 * 常见字符串工具类
 *
 * @author TaoYu
 */
public abstract class DsStrUtils {

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 字符串
     * @return true: null or "" or "   "  false: "a"
     */
    public static boolean hasLength(CharSequence str) {
        return (str != null && str.length() > 0);
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 字符串
     * @return true: null or "" or "   "  false: "a"
     */
    public static boolean hasLength(String str) {
        return hasLength((CharSequence) str);
    }

    /**
     * 判断字符串是否有内容
     *
     * @param str 字符串
     * @return true: null or "" or "   "  false: "a"
     */
    public static boolean hasText(CharSequence str) {
        if (!hasLength(str)) {
            return false;
        }

        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断字符串是否有内容, 没内容就返回默认值
     *
     * @param str        字符串
     * @param defaultStr 默认值
     * @return
     */
    public static String defaultIfBlank(final String str, final String defaultStr) {
        return !hasText(str) ? defaultStr : str;
    }
}