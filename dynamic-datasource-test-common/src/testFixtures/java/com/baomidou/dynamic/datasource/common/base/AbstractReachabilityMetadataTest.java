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
package com.baomidou.dynamic.datasource.common.base;

import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DatasourceInitProperties;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 在 JVM 下直接校验 dynamic-datasource-native-metadata 为配置绑定登记的反射元数据。
 * <p>
 * Boot 的 AOT 不为 {@link DatasourceInitProperties} 生成绑定 hints，原生镜像下 init 块能否绑定全靠这份元数据（#809）。
 * 光靠 nativeTest 守不住它：所有测试类共用一个原生进程，typeReached 条件一旦被先启动的上下文满足便一直有效。
 * 以 DynamicDataSourceRefresher 为条件的条目就会被更早的测试上下文激活，掩盖单上下文应用启动时的
 * MissingReflectionRegistrationError。
 * <p>
 * 只有 Boot3/4 的 starter 依赖 dynamic-datasource-native-metadata，故只由 v3 / v4 的薄子类继承本类。
 */
public abstract class AbstractReachabilityMetadataTest {

    private static final String METADATA = "META-INF/native-image/com.baomidou/dynamic-datasource-native-metadata/reachability-metadata.json";

    /**
     * 绑定 {@code DataSourceProperty#init} 时，已经初始化的只有它自身与所属的 {@link DataSourceProperty}，
     * 条件落在别处（例如要等属性绑定完才创建的 bean）就来不及生效。
     */
    private static final Collection<String> REACHED_BEFORE_BINDING = Arrays.asList(
            DatasourceInitProperties.class.getName(), DataSourceProperty.class.getName());

    /**
     * {@link DatasourceInitProperties} 的无参构造与全部属性的 getter / setter 必须登记在绑定前就已生效的条件下。
     * 预期集合由 {@link Introspector} 现场推导，新增属性而漏改元数据时同样会失败。
     */
    @Test
    void datasourceInitPropertiesShouldBeRegisteredBeforeBinding() throws IOException, IntrospectionException {
        Set<String> expected = new TreeSet<>();
        expected.add(signature("<init>", new Class<?>[0]));
        for (PropertyDescriptor descriptor : Introspector.getBeanInfo(DatasourceInitProperties.class, Object.class).getPropertyDescriptors()) {
            for (Method method : Arrays.asList(descriptor.getReadMethod(), descriptor.getWriteMethod())) {
                if (method != null) {
                    expected.add(signature(method.getName(), method.getParameterTypes()));
                }
            }
        }
        Set<String> registered = new TreeSet<>();
        for (Map<String, Object> entry : entriesOf(DatasourceInitProperties.class)) {
            if (isReachedBeforeBinding(entry)) {
                registered.addAll(executablesOf(DatasourceInitProperties.class, entry));
            }
        }
        assertThat(registered).as("条件为 %s 之一的条目所登记的方法", REACHED_BEFORE_BINDING).containsAll(expected);
    }

    private static List<Map<String, Object>> entriesOf(Class<?> type) throws IOException {
        try (InputStream in = new ClassPathResource(METADATA).getInputStream()) {
            String json = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
            return JsonPath.read(json, "$.reflection[?(@.type == '" + type.getName() + "')]");
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean isReachedBeforeBinding(Map<String, Object> entry) {
        Map<String, Object> condition = (Map<String, Object>) entry.get("condition");
        return condition == null || REACHED_BEFORE_BINDING.contains(condition.get("typeReached"));
    }

    /**
     * 把条目登记的构造与方法统一成签名，显式列出的 methods 与 all*Methods / all*Constructors 开关都计入。
     */
    @SuppressWarnings("unchecked")
    private static Set<String> executablesOf(Class<?> type, Map<String, Object> entry) {
        Set<String> signatures = new TreeSet<>();
        List<Map<String, Object>> methods = (List<Map<String, Object>>) entry.get("methods");
        if (methods != null) {
            for (Map<String, Object> method : methods) {
                StringJoiner parameterTypes = new StringJoiner(",", method.get("name") + "(", ")");
                for (Object parameterType : (List<Object>) method.get("parameterTypes")) {
                    parameterTypes.add(String.valueOf(parameterType));
                }
                signatures.add(parameterTypes.toString());
            }
        }
        if (Boolean.TRUE.equals(entry.get("allPublicMethods"))) {
            for (Method method : type.getMethods()) {
                signatures.add(signature(method.getName(), method.getParameterTypes()));
            }
        }
        if (Boolean.TRUE.equals(entry.get("allDeclaredMethods"))) {
            for (Method method : type.getDeclaredMethods()) {
                signatures.add(signature(method.getName(), method.getParameterTypes()));
            }
        }
        if (Boolean.TRUE.equals(entry.get("allPublicConstructors"))) {
            for (Constructor<?> constructor : type.getConstructors()) {
                signatures.add(signature("<init>", constructor.getParameterTypes()));
            }
        }
        if (Boolean.TRUE.equals(entry.get("allDeclaredConstructors"))) {
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                signatures.add(signature("<init>", constructor.getParameterTypes()));
            }
        }
        return signatures;
    }

    private static String signature(String name, Class<?>[] parameterTypes) {
        StringJoiner signature = new StringJoiner(",", name + "(", ")");
        for (Class<?> parameterType : parameterTypes) {
            signature.add(parameterType.getTypeName());
        }
        return signature.toString();
    }
}
