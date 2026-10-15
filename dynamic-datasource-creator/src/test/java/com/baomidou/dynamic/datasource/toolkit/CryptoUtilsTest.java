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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 加密工具类单元测试：验证 {@code ENC(...)} 密码背后"私钥加密 / 公钥解密"这一基本功能。
 * <p>
 * 注意本库的约定与常规非对称加密相反：用<b>私钥</b>加密（{@link CryptoUtils#encrypt}），
 * 用<b>公钥</b>解密（{@link CryptoUtils#decrypt}），因此应用侧只需配置公钥即可解密配置文件里的密文。
 *
 * @author TaoYu
 */
class CryptoUtilsTest {

    @Test
    void defaultKeyPairShouldRoundTripShortText() throws Exception {
        String plain = "123456";
        String cipher = CryptoUtils.encrypt(plain);
        assertThat(cipher).isNotBlank().isNotEqualTo(plain);
        assertThat(CryptoUtils.decrypt(cipher)).isEqualTo(plain);
    }

    @Test
    void defaultKeyPairShouldRoundTripTextLongerThanOneBlock() throws Exception {
        // 内置默认密钥是 512 位，单块明文上限 = 64 - 11 = 53；构造超过单块长度以覆盖分段加密/解密分支
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append("dynamic-datasource");
        }
        String plain = sb.toString();
        assertThat(plain.length()).isGreaterThan(53);

        String cipher = CryptoUtils.encrypt(plain);
        assertThat(CryptoUtils.decrypt(cipher)).isEqualTo(plain);
    }

    @Test
    void customKeyPairShouldRoundTrip() throws Exception {
        String[] keyPair = CryptoUtils.genKeyPair();
        String privateKey = keyPair[0];
        String publicKey = keyPair[1];
        assertThat(privateKey).isNotBlank();
        assertThat(publicKey).isNotBlank();

        String plain = "p@ssw0rd-with-custom-key";
        String cipher = CryptoUtils.encrypt(privateKey, plain);
        assertThat(CryptoUtils.decrypt(publicKey, cipher)).isEqualTo(plain);
    }

    @Test
    void decryptShouldReturnInputWhenBlank() throws Exception {
        assertThat(CryptoUtils.decrypt("")).isEmpty();
        assertThat(CryptoUtils.decrypt((String) null)).isNull();
    }

    @Test
    void getPublicKeyShouldFallBackToDefaultWhenBlank() {
        // 空/ null 公钥回退到内置默认公钥，不应抛异常
        assertThat(CryptoUtils.getPublicKey(null)).isNotNull();
        assertThat(CryptoUtils.getPublicKey("")).isNotNull();
        assertThat(CryptoUtils.getPublicKey(CryptoUtils.DEFAULT_PUBLIC_KEY_STRING)).isNotNull();
    }

    @Test
    void getPublicKeyShouldRejectGarbage() {
        // 非法公钥字符串应被包装为 IllegalArgumentException
        assertThatThrownBy(() -> CryptoUtils.getPublicKey("not-a-real-key"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("public key");
    }
}
