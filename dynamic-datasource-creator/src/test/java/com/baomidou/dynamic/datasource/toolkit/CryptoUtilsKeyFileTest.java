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
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link CryptoUtils} 文件密钥路径的单元测试：通过公钥文件读取公钥、
 * X509 文件解析（null/空回退 default）、{@code genKeyPairBytes} 生成结果。
 *
 * @author TaoYu
 */
class CryptoUtilsKeyFileTest {

    @TempDir
    Path tempDir;

    @Test
    void getPublicKeyByPublicKeyFileShouldFallbackToDefaultWhenNull() {
        // null/空 → 回退内置公钥
        assertThat(CryptoUtils.getPublicKeyByPublicKeyFile(null)).isNotNull();
        assertThat(CryptoUtils.getPublicKeyByPublicKeyFile("")).isNotNull();
    }

    @Test
    void getPublicKeyByPublicKeyFileShouldReadValidX509KeyFromDisk() throws Exception {
        byte[][] keyPair = CryptoUtils.genKeyPairBytes();
        byte[] publicKeyBytes = keyPair[1]; // X509 encoded public key

        Path keyFile = tempDir.resolve("pub.key");
        try (OutputStream os = Files.newOutputStream(keyFile)) {
            os.write(publicKeyBytes);
        }

        PublicKey pk = CryptoUtils.getPublicKeyByPublicKeyFile(keyFile.toAbsolutePath().toString());
        assertThat(pk).isNotNull();
        assertThat(pk.getAlgorithm()).isEqualTo("RSA");
    }

    @Test
    void getPublicKeyByPublicKeyFileShouldThrowForNonexistentFile() {
        assertThatThrownBy(() -> CryptoUtils.getPublicKeyByPublicKeyFile("/no/such/file.key"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("public key");
    }

    @Test
    void getPublicKeyByX509ShouldFallbackToDefaultWhenNull() {
        assertThat(CryptoUtils.getPublicKeyByX509(null)).isNotNull();
        assertThat(CryptoUtils.getPublicKeyByX509("")).isNotNull();
    }

    @Test
    void getPublicKeyByX509ShouldThrowForNonexistentFile() {
        assertThatThrownBy(() -> CryptoUtils.getPublicKeyByX509("/no/such/cert.crt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("public key");
    }

    @Test
    void getPublicKeyByX509ShouldThrowForGarbageBytes() throws IOException {
        Path bad = tempDir.resolve("bad.crt");
        Files.write(bad, "not a certificate".getBytes());
        assertThatThrownBy(() -> CryptoUtils.getPublicKeyByX509(bad.toAbsolutePath().toString()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void genKeyPairBytesShouldReturnValidPair() {
        byte[][] kp = CryptoUtils.genKeyPairBytes();
        assertThat(kp.length).isEqualTo(2);
        assertThat(kp[0]).isNotEmpty(); // PKCS8 private key
        assertThat(kp[1]).isNotEmpty(); // X509 public key
    }

    @Test
    void genKeyPairBytesShouldRejectWeakKeySize() {
        assertThatThrownBy(() -> CryptoUtils.genKeyPairBytes(512))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2048");
        assertThatThrownBy(() -> CryptoUtils.genKeyPair(1024))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2048");
    }

    @Test
    void genKeyPairBytesShouldProduceRoundTrippablePair() throws Exception {
        byte[][] kp = CryptoUtils.genKeyPairBytes();
        String privBase64 = Base64.byteArrayToBase64(kp[0]);
        String pubBase64 = Base64.byteArrayToBase64(kp[1]);
        String plain = "round-trip-test";
        String cipher = CryptoUtils.encrypt(privBase64, plain);
        assertThat(CryptoUtils.decrypt(pubBase64, cipher)).isEqualTo(plain);
    }

    @Test
    void builtInDefaultKeyPairShouldStillRoundTrip() throws Exception {
        // 内置默认密钥对（Druid 那把 512 位）必须保持可解密，否则存量配置全废
        String plain = "root-password";
        assertThat(CryptoUtils.decrypt(CryptoUtils.encrypt(plain))).isEqualTo(plain);
    }

    @Test
    void longPlainTextShouldRoundTripInBlocks() throws Exception {
        // 超出单块长度时必须分段加解密：块大小按模长推导，2048 位下不能被写死的 53/64 截断
        String[] pairs = CryptoUtils.genKeyPair();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("jdbc:mysql://host-").append(i).append(":3306/db;");
        }
        assertThat(CryptoUtils.decrypt(pairs[1], CryptoUtils.encrypt(pairs[0], sb.toString())))
                .isEqualTo(sb.toString());
    }
}
