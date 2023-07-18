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


public enum DsPropagation {
    /**
     * 支持当前事务，如果当前没有事务，就新建一个事务。这是最常见的选择。
     */
    REQUIRED,
    /**
     * 新建事务，如果当前存在事务，把当前事务挂起。
     */
    REQUIRES_NEW,
    /**
     * 以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。
     */
    NOT_SUPPORTED,
    /**
     * 支持当前事务，如果当前没有事务，就以非事务方式执行。
     */
    SUPPORTS,
    /**
     * 以非事务方式执行，如果当前存在事务，则抛出异常。
     */
    NEVER,
    /**
     * 支持当前事务，如果当前没有事务，就抛出异常。
     */
    MANDATORY,
    /**
     * 如果当前存在事务，则在嵌套事务内执行，如果当前没有事务，就新建一个事务。
     */
    NESTED
}