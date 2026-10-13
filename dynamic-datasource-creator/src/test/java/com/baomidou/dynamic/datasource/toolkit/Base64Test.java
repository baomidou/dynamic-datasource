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

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Base64 工具类单元测试，核心断言是其标准编码与 JDK {@link java.util.Base64} 完全等价。
 *
 * @author TaoYu
 */
class Base64Test {

    @Test
    void standardEncodingShouldMatchJdkBase64() {
        byte[][] samples = {
                "".getBytes(StandardCharsets.UTF_8),
                "f".getBytes(StandardCharsets.UTF_8),
                "fo".getBytes(StandardCharsets.UTF_8),
                "foo".getBytes(StandardCharsets.UTF_8),
                "fooo".getBytes(StandardCharsets.UTF_8),
                // 触发 '+' 与 '/' 特殊字符
                new byte[]{(byte) 0xFB, (byte) 0xFF, (byte) 0xBF},
        };
        for (byte[] s : samples) {
            assertThat(Base64.byteArrayToBase64(s))
                    .isEqualTo(java.util.Base64.getEncoder().encodeToString(s));
        }
    }

    @Test
    void standardRoundTripShouldRecoverOriginal() {
        Random random = new Random(42);
        for (int len = 0; len < 64; len++) {
            byte[] data = new byte[len];
            random.nextBytes(data);
            byte[] decoded = Base64.base64ToByteArray(Base64.byteArrayToBase64(data));
            assertThat(decoded).isEqualTo(data);
        }
    }

    @Test
    void alternateAlphabetRoundTripShouldRecoverOriginal() {
        byte[] data = "dynamic-datasource-alt-base64".getBytes(StandardCharsets.UTF_8);
        String encoded = Base64.byteArrayToAltBase64(data);
        assertThat(encoded).isNotEqualTo(Base64.byteArrayToBase64(data));
        assertThat(Base64.altBase64ToByteArray(encoded)).isEqualTo(data);
    }

    @Test
    void decodeShouldRejectLengthNotMultipleOfFour() {
        assertThatThrownBy(() -> Base64.base64ToByteArray("abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("multiple of four");
    }

    @Test
    void emptyInputShouldProduceEmptyOutput() {
        assertThat(Base64.byteArrayToBase64(new byte[0])).isEmpty();
        assertThat(Base64.base64ToByteArray("")).isEmpty();
    }
}
