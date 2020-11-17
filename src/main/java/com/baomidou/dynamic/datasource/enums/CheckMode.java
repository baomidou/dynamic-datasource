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
package com.baomidou.dynamic.datasource.enums;

public enum CheckMode {
    /**
     * 严格模式
     * 无法匹配数据源时，抛出异常
     */
    STRICT,
    /**
     * 调用栈日志模式
     * 无法匹配数据源时，打印日志
     */
    STACK_LOG,
    /**
     * 普通日志模式
     * 无法匹配数据源时，打印日志
     */
    LOG,
    /**
     * 无法匹配数据源时, 什么也不做,默认选择主数据源
     */
    NONE;
}
