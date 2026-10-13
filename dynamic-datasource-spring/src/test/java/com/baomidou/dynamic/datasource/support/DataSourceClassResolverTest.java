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
package com.baomidou.dynamic.datasource.support;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.dynamic.datasource.tx.DsPropagation;
import com.baomidou.dynamic.datasource.tx.TransactionalInfo;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DataSourceClassResolver} 注解解析优先级的单元测试：方法级 {@code @DS}、类级 {@code @DS}、
 * {@code allowedPublicOnly} 对私有方法的取舍、Object 方法短路、以及 {@code @DSTransactional} 事务信息解析与缓存。
 *
 * @author TaoYu
 */
class DataSourceClassResolverTest {

    @DS("methodDs")
    public static class MethodAnnotatedTarget {
        public void probe() {
        }
    }

    @DS("classDs")
    public static class ClassAnnotatedTarget {
        public void probe() {
        }
    }

    public static class MixedTarget {
        @DS("priv")
        private void privateAnnotated() {
        }

        @DS("tx")
        @DSTransactional(propagation = DsPropagation.REQUIRES_NEW)
        public void txMethod() {
        }

        public void unannotated() {
        }
    }

    private static Method method(Class<?> clazz, String name, Class<?>... params) {
        try {
            return clazz.getDeclaredMethod(name, params);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void methodLevelDsShouldBeResolved() {
        DataSourceClassResolver resolver = new DataSourceClassResolver(true);
        String key = resolver.findKey(method(MethodAnnotatedTarget.class, "probe"),
                new MethodAnnotatedTarget(), DS.class);
        assertThat(key).isEqualTo("methodDs");
    }

    @Test
    void classLevelDsShouldBeResolvedWhenMethodHasNone() {
        DataSourceClassResolver resolver = new DataSourceClassResolver(true);
        String key = resolver.findKey(method(ClassAnnotatedTarget.class, "probe"),
                new ClassAnnotatedTarget(), DS.class);
        assertThat(key).isEqualTo("classDs");
    }

    @Test
    void objectMethodShouldShortCircuitToEmpty() {
        DataSourceClassResolver resolver = new DataSourceClassResolver(true);
        String key = resolver.findKey(method(Object.class, "toString"),
                new ClassAnnotatedTarget(), DS.class);
        assertThat(key).isEmpty();
    }

    @Test
    void privateAnnotatedMethodHonoursAllowedPublicOnly() {
        String key = new DataSourceClassResolver(true)
                .findKey(method(MixedTarget.class, "privateAnnotated"), new MixedTarget(), DS.class);
        assertThat(key).as("allowedPublicOnly=true 时私有方法被忽略").isEmpty();

        String keyPublicAllowed = new DataSourceClassResolver(false)
                .findKey(method(MixedTarget.class, "privateAnnotated"), new MixedTarget(), DS.class);
        assertThat(keyPublicAllowed).as("allowedPublicOnly=false 时私有方法可解析").isEqualTo("priv");
    }

    @Test
    void unannotatedMethodShouldReturnEmpty() {
        DataSourceClassResolver resolver = new DataSourceClassResolver(true);
        String key = resolver.findKey(method(MixedTarget.class, "unannotated"), new MixedTarget(), DS.class);
        assertThat(key).isEmpty();
    }

    @Test
    void transactionalInfoShouldBeResolvedFromAnnotation() {
        DataSourceClassResolver resolver = new DataSourceClassResolver(true);
        TransactionalInfo info = resolver.findTransactionalInfo(
                method(MixedTarget.class, "txMethod"), new MixedTarget(), DSTransactional.class);
        assertThat(info).isNotNull();
        assertThat(info.getPropagation()).isEqualTo(DsPropagation.REQUIRES_NEW);
    }

    @Test
    void transactionalInfoShouldBeCachedAsSameInstance() {
        DataSourceClassResolver resolver = new DataSourceClassResolver(true);
        Method m = method(MixedTarget.class, "txMethod");
        MixedTarget target = new MixedTarget();
        TransactionalInfo first = resolver.findTransactionalInfo(m, target, DSTransactional.class);
        TransactionalInfo second = resolver.findTransactionalInfo(m, target, DSTransactional.class);
        // 命中缓存：两次返回同一实例
        assertThat(first).isSameAs(second);
    }

    @Test
    void missingTransactionalAnnotationShouldYieldNullAttribute() {
        DataSourceClassResolver resolver = new DataSourceClassResolver(true);
        TransactionalInfo info = resolver.findTransactionalInfo(
                method(MixedTarget.class, "unannotated"), new MixedTarget(), DSTransactional.class);
        assertThat(info.toString()).isEqualTo("null");
    }
}
