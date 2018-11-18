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
package com.baomidou.dynamic.datasource.spel;

import org.aopalliance.intercept.MethodInvocation;

/**
 * 动态数据源SPEL解析器
 *
 * @author TaoYu
 * @since 2.3.3
 */
public interface DynamicDataSourceSpelParser {

    /**
     * 解析SPEL
     *
     * @param invocation 动态代理方法对象
     * @param key        ds的值
     * @return 解析SPEL后获取的数据源名称
     */
    String parse(MethodInvocation invocation, String key);
}
