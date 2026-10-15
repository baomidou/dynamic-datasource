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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据源配置指纹：把 {@code DataSourceProperty}（含其嵌套的池配置）折算成一个稳定的字符串，
 * 用于判断「这一份配置相对上一次是否真的变了」。
 * <p>
 * 之所以需要它：配置中心刷新时拿到的 {@code DataSourceProperty} 与启动时那个对象不是同一实例，
 * 而 Spring 的 rebind 可能原地改写既有对象，直接持有引用做 diff 并不可靠。指纹是不可变值，更安全。
 * <p>
 * 设计取舍：
 * <ul>
 *     <li>超过 {@link #MAX_DEPTH} 的嵌套只输出类名——<b>宁可漏检也不误判</b>，误判就等于全量重建连接池；</li>
 *     <li>Map 按 key 排序输出，避免迭代顺序差异造成假变更；</li>
 *     <li>getter 按名字排序，规避 {@link Introspector} 返回顺序不稳定的问题。</li>
 * </ul>
 *
 * @author TaoYu
 * @since 4.7.0
 */
public final class DsPropertyFingerprint {

    /**
     * 递归深度上限，超出后不再展开对象内容
     */
    private static final int MAX_DEPTH = 4;

    /**
     * null、空串、空集合在指纹里一律等价。
     * <p>
     * 启动链路（provider 的名称兜底、creator 的全局值回填、各池创建器的默认值）会把原本为 null 的字段
     * 填成空容器，而配置中心推来的等价配置里这些字段仍是 null。这类「无实质差别」若不抹平，
     * 每次刷新都会被判成全量变更——那正是本指纹要避免的事。
     */
    private static final String EMPTY = "<empty>";

    private DsPropertyFingerprint() {
    }

    /**
     * 计算配置指纹
     *
     * @param target   目标对象，通常是 {@code DataSourceProperty}
     * @param excluded 需要排除的属性名（用于「归一化后再比对」的字段，如 poolName / lazy / publicKey）
     * @return 指纹字符串，相同配置必定相同
     */
    public static String of(Object target, String... excluded) {
        Set<String> skip = excluded == null || excluded.length == 0
                ? java.util.Collections.emptySet() : new HashSet<>(Arrays.asList(excluded));
        StringBuilder sb = new StringBuilder(256);
        append(sb, target, 0, skip);
        return sb.toString();
    }

    private static void append(StringBuilder sb, Object target, int depth, Set<String> excluded) {
        if (target == null) {
            sb.append(EMPTY);
            return;
        }
        if (target instanceof Class) {
            sb.append(((Class<?>) target).getName());
            return;
        }
        if (target instanceof CharSequence) {
            sb.append(((CharSequence) target).length() == 0 ? EMPTY : target);
            return;
        }
        if (target instanceof Number || target instanceof Boolean
                || target instanceof Character || target instanceof Enum<?>) {
            sb.append(target);
            return;
        }
        if (depth >= MAX_DEPTH) {
            // 不展开内容，避免因对象身份差异被误判成变更
            sb.append(target.getClass().getName()).append("{}");
            return;
        }
        if (target instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) target;
            if (map.isEmpty()) {
                sb.append(EMPTY);
                return;
            }
            appendMap(sb, map, depth, excluded);
            return;
        }
        if (target instanceof Iterable) {
            Iterator<?> iterator = ((Iterable<?>) target).iterator();
            if (!iterator.hasNext()) {
                sb.append(EMPTY);
                return;
            }
            sb.append('[');
            while (iterator.hasNext()) {
                append(sb, iterator.next(), depth + 1, excluded);
                sb.append('|');
            }
            sb.append(']');
            return;
        }
        if (target.getClass().isArray()) {
            int length = Array.getLength(target);
            if (length == 0) {
                sb.append(EMPTY);
                return;
            }
            sb.append('[');
            for (int i = 0; i < length; i++) {
                append(sb, Array.get(target, i), depth + 1, excluded);
                sb.append('|');
            }
            sb.append(']');
            return;
        }
        appendBean(sb, target, depth, excluded);
    }

    private static void appendMap(StringBuilder sb, Map<?, ?> map, int depth, Set<String> excluded) {
        List<Map.Entry<?, ?>> entries = new ArrayList<>(map.entrySet());
        entries.sort(Comparator.comparing(e -> String.valueOf(e.getKey())));
        sb.append('{');
        for (Map.Entry<?, ?> entry : entries) {
            sb.append(String.valueOf(entry.getKey())).append('=');
            append(sb, entry.getValue(), depth + 1, excluded);
            sb.append('|');
        }
        sb.append('}');
    }

    private static void appendBean(StringBuilder sb, Object target, int depth, Set<String> excluded) {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(target.getClass(), Object.class);
        } catch (IntrospectionException e) {
            sb.append(target.getClass().getName()).append("{}");
            return;
        }
        List<PropertyDescriptor> descriptors = new ArrayList<>(Arrays.asList(beanInfo.getPropertyDescriptors()));
        descriptors.sort(Comparator.comparing(PropertyDescriptor::getName));
        sb.append(target.getClass().getSimpleName()).append('{');
        for (PropertyDescriptor descriptor : descriptors) {
            String name = descriptor.getName();
            if (excluded.contains(name)) {
                continue;
            }
            Method readMethod = descriptor.getReadMethod();
            if (readMethod == null || readMethod.getParameterCount() > 0) {
                continue;
            }
            sb.append(name).append('=');
            Object value;
            try {
                value = readMethod.invoke(target);
            } catch (Exception e) {
                sb.append("<unreadable>");
                continue;
            }
            append(sb, value, depth + 1, excluded);
            sb.append('|');
        }
        sb.append('}');
    }
}
