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
package com.baomidou.dynamic.datasource.aop;

import com.baomidou.dynamic.datasource.tx.ConnectionFactory;
import com.baomidou.dynamic.datasource.tx.TransactionContext;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.util.UUID;

/**
 * @author funkye
 */
public class DynamicTransactionAdvisor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if (StringUtils.isNotBlank(TransactionContext.getXID())) {
            return methodInvocation.proceed();
        }
        boolean state = true;
        Object o;
        String xid = UUID.randomUUID().toString();
        TransactionContext.bind(xid);
        try {
            o = methodInvocation.proceed();
        } catch (Exception e) {
            state = false;
            throw e;
        } finally {
            ConnectionFactory.notify(xid, state);
            TransactionContext.remove();
        }
        return o;
    }
}
