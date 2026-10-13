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

import org.junit.jupiter.api.Test;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DsConfigUtil} 配置工具的纯逻辑单元测试：横划线转驼峰、配置合并（当前覆盖全局）、
 * 属性描述符/getter/setter 反射收集、以及把字符串值转换成 setter 目标类型的全部分支。
 *
 * @author TaoYu
 */
class DsConfigUtilTest {

    /** 承载各种 setter 参数类型，供 convertValue 分支验证。 */
    @SuppressWarnings("unused")
    public static class Bean {
        private String s;
        private int i;
        private Integer io;
        private long l;
        private Long lo;
        private boolean b;
        private Boolean bo;
        private double d;
        private Double dobj;
        private float f;

        public void setS(String v) { s = v; }
        public String getS() { return s; }
        public void setI(int v) { i = v; }
        public void setIo(Integer v) { io = v; }
        public void setL(long v) { l = v; }
        public void setLo(Long v) { lo = v; }
        public void setB(boolean v) { b = v; }
        public void setBo(Boolean v) { bo = v; }
        public void setD(double v) { d = v; }
        public void setDobj(Double v) { dobj = v; }
        public void setF(float v) { f = v; }

        public void twoArgs(String a, int b) { }
        public void custom(Bean other) { }
    }

    @Test
    void lineToUpperShouldConvertKebabToCamel() {
        assertThat(DsConfigUtil.lineToUpper("connection-test-query")).isEqualTo("connectionTestQuery");
        assertThat(DsConfigUtil.lineToUpper("max-pool-size")).isEqualTo("maxPoolSize");
    }

    @Test
    void lineToUpperShouldKeepPlainString() {
        assertThat(DsConfigUtil.lineToUpper("poolName")).isEqualTo("poolName");
        assertThat(DsConfigUtil.lineToUpper("")).isEmpty();
    }

    @Test
    void mergeMapShouldLetCurrentOverrideGlobal() {
        Map<String, Object> global = new HashMap<>();
        global.put("a", 1);
        global.put("b", 2);
        Map<String, Object> current = new HashMap<>();
        current.put("b", 3);
        current.put("c", 4);

        Map<String, Object> merged = DsConfigUtil.mergeMap(current, global);

        assertThat(merged).containsEntry("a", 1).containsEntry("b", 3).containsEntry("c", 4);
    }

    @Test
    void propertyIntrospectionShouldExposeAccessors() {
        Map<String, PropertyDescriptor> pdMap = DsConfigUtil.getPropertyDescriptorMap(Bean.class);
        assertThat(pdMap).containsKey("s").doesNotContainKey("class");

        Map<String, Method> getters = DsConfigUtil.getGetterMethods(Bean.class);
        assertThat(getters).containsKey("s");

        Map<String, Method> setters = DsConfigUtil.getSetterMethods(Bean.class);
        assertThat(setters).containsKeys("i", "l", "b", "d", "f");
    }

    @Test
    void convertValueShouldCoerceEachSupportedType() throws Exception {
        assertThat(DsConfigUtil.convertValue(Bean.class.getMethod("setS", String.class), 123))
                .isEqualTo("123");
        assertThat(DsConfigUtil.convertValue(Bean.class.getMethod("setI", int.class), "45"))
                .isEqualTo(45);
        assertThat(DsConfigUtil.convertValue(Bean.class.getMethod("setIo", Integer.class), "46"))
                .isEqualTo(46);
        assertThat(DsConfigUtil.convertValue(Bean.class.getMethod("setL", long.class), "7"))
                .isEqualTo(7L);
        assertThat(DsConfigUtil.convertValue(Bean.class.getMethod("setLo", Long.class), "8"))
                .isEqualTo(8L);
        assertThat(DsConfigUtil.convertValue(Bean.class.getMethod("setB", boolean.class), "true"))
                .isEqualTo(true);
        assertThat(DsConfigUtil.convertValue(Bean.class.getMethod("setBo", Boolean.class), "false"))
                .isEqualTo(false);
        assertThat(DsConfigUtil.convertValue(Bean.class.getMethod("setD", double.class), "1.5"))
                .isEqualTo(1.5);
        assertThat(DsConfigUtil.convertValue(Bean.class.getMethod("setDobj", Double.class), "2.5"))
                .isEqualTo(2.5);
        assertThat(DsConfigUtil.convertValue(Bean.class.getMethod("setF", float.class), "3.5"))
                .isEqualTo(3.5f);
    }

    @Test
    void convertValueShouldPassThroughForUnsupportedShapes() throws Exception {
        Object sentinel = new Object();
        // 多参数方法：原样返回
        assertThat(DsConfigUtil.convertValue(Bean.class.getMethod("twoArgs", String.class, int.class), sentinel))
                .isSameAs(sentinel);
        // 单参数但类型不在支持列表：原样返回
        assertThat(DsConfigUtil.convertValue(Bean.class.getMethod("custom", Bean.class), sentinel))
                .isSameAs(sentinel);
    }
}
