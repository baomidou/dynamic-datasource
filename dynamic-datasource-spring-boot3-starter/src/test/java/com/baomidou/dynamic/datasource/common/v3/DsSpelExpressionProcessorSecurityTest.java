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
package com.baomidou.dynamic.datasource.common.v3;

import com.baomidou.dynamic.datasource.processor.DsSpelExpressionProcessor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.expression.EvaluationException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Security tests for {@link DsSpelExpressionProcessor} verifying that SpEL injection via
 * type references (T(...)) is blocked to prevent Remote Code Execution, and that the
 * behaviour can be restored by explicitly enabling {@code allowedSpelTypeAccess}.
 */
class DsSpelExpressionProcessorSecurityTest {

    private DsSpelExpressionProcessor processor;
    private MethodInvocation invocation;

    @BeforeEach
    void setUp() throws Exception {
        processor = new DsSpelExpressionProcessor();

        Method method = SampleService.class.getMethod("getByTenant", String.class);
        invocation = mock(MethodInvocation.class);
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getArguments()).thenReturn(new Object[]{"tenant1"});
        when(invocation.getThis()).thenReturn(new SampleService());
    }

    @Test
    void normalSpelExpressionShouldWork() {
        // #tenant reads the method parameter value normally
        String result = processor.doDetermineDatasource(invocation, "#tenant");
        assertEquals("tenant1", result);
    }

    @Test
    void typeReferenceExpressionShouldBeBlockedByDefault() {
        // T(...) type references must be blocked to prevent SpEL injection / RCE
        assertThrows(EvaluationException.class, () ->
                processor.doDetermineDatasource(invocation, "T(java.lang.Runtime).getRuntime().exec('id')")
        );
    }

    @Test
    void newInstanceExpressionShouldBeBlockedByDefault() {
        // new Type(...) constructor invocations must also be blocked
        assertThrows(EvaluationException.class, () ->
                processor.doDetermineDatasource(invocation, "new java.lang.ProcessBuilder('id').start()")
        );
    }

    @Test
    void typeReferenceExpressionShouldWorkWhenExplicitlyAllowed() {
        // When allowedSpelTypeAccess=true, T(...) expressions are permitted (opt-in unsafe mode)
        processor.setAllowedSpelTypeAccess(true);
        // T(java.lang.String) is a safe type reference to verify the restriction is lifted
        String result = processor.doDetermineDatasource(invocation, "T(java.lang.String).valueOf(#tenant)");
        assertEquals("tenant1", result);
    }

    static class SampleService {
        public String getByTenant(String tenant) {
            return tenant;
        }
    }
}
