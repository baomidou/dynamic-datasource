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
package com.baomidou.dynamic.datasource.event;

import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.exception.ErrorCreateDataSourceException;
import com.baomidou.dynamic.datasource.toolkit.CryptoUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link EncDataSourceInitEvent} 配置解密（{@code ENC(...)}）基本功能的单元测试。
 * <p>
 * 复用 {@link CryptoUtils} "私钥加密 / 公钥解密" 约定：用私钥生成密文，塞进 url/username/password
 * 的 {@code ENC(...)} 包裹中，事件在数据源创建前用公钥还原明文。
 *
 * @author TaoYu
 */
class EncDataSourceInitEventTest {

    private final EncDataSourceInitEvent event = new EncDataSourceInitEvent();

    @Test
    void shouldDecryptEncWrappedCredentialsWithPublicKey() throws Exception {
        String[] keyPair = CryptoUtils.genKeyPair();
        String privateKey = keyPair[0];
        String publicKey = keyPair[1];

        DataSourceProperty property = new DataSourceProperty();
        property.setPublicKey(publicKey);
        property.setUrl("ENC(" + CryptoUtils.encrypt(privateKey, "jdbc:h2:mem:enc") + ")");
        property.setUsername("ENC(" + CryptoUtils.encrypt(privateKey, "sa") + ")");
        property.setPassword("ENC(" + CryptoUtils.encrypt(privateKey, "p@ss") + ")");

        event.beforeCreate(property);

        assertThat(property.getUrl()).isEqualTo("jdbc:h2:mem:enc");
        assertThat(property.getUsername()).isEqualTo("sa");
        assertThat(property.getPassword()).isEqualTo("p@ss");
    }

    @Test
    void plainValuesWithoutEncShouldPassThrough() throws Exception {
        String[] keyPair = CryptoUtils.genKeyPair();
        String publicKey = keyPair[1];

        DataSourceProperty property = new DataSourceProperty();
        property.setPublicKey(publicKey);
        property.setUrl("jdbc:h2:mem:plain");
        property.setUsername("sa");
        property.setPassword("plain-pwd");

        event.beforeCreate(property);

        // 无 ENC() 包裹的明文保持原样，不会被误解密
        assertThat(property.getUrl()).isEqualTo("jdbc:h2:mem:plain");
        assertThat(property.getUsername()).isEqualTo("sa");
        assertThat(property.getPassword()).isEqualTo("plain-pwd");
    }

    @Test
    void decryptionFailureWithWrongKeyShouldThrowInsteadOfReturningCiphertext() throws Exception {
        // 旧实现吞异常返回密文原文：应用带着字面量 "ENC(...)" 当密码启动，
        // 报出来的是误导性的 access denied，真因（公钥不匹配/密钥长度）被埋在第一层日志。
        String[] encKeyPair = CryptoUtils.genKeyPair();
        String[] wrongKeyPair = CryptoUtils.genKeyPair();

        DataSourceProperty property = new DataSourceProperty();
        property.setPublicKey(wrongKeyPair[1]);
        property.setPassword("ENC(" + CryptoUtils.encrypt(encKeyPair[0], "secret") + ")");

        assertThatThrownBy(() -> event.beforeCreate(property))
                .isInstanceOf(ErrorCreateDataSourceException.class)
                .hasMessageContaining("public-key");
    }

    @Test
    void noPublicKeyShouldSkipDecryption() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("ENC(not-really-encrypted)");

        event.beforeCreate(property);

        // 未配置公钥时整个解密流程跳过，url 原样保留
        assertThat(property.getUrl()).isEqualTo("ENC(not-really-encrypted)");
    }
}
