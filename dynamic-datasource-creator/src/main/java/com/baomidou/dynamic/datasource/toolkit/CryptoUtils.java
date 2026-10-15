package com.baomidou.dynamic.datasource.toolkit;

import javax.crypto.Cipher;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author alibaba
 * <p>
 * 实现 copy 自 Druid 的 {@code ConfigTools}，密文格式与之保持一致（私钥加密 / 公钥解密 + PKCS#1 v1.5），
 * 以便直接沿用 Druid 生成的配置密文。
 * <p>
 * <b>安全模型说明</b>：这是"配置混淆"而非机密性保护——公钥即可解密，且内置的默认密钥对是 Druid
 * 公开源码里那把 512 位 RSA，任何人都能解开用默认密钥加密的内容。生产环境请务必用
 * {@link #genKeyPair()} 生成自己的密钥对，并把公钥配置到 {@code public-key}。
 */
@Slf4j
public class CryptoUtils {

    public static final String DEFAULT_PUBLIC_KEY_STRING = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJ4o6sn4WoPmbs7DR9mGQzuuUQM9erQTVPpwxIzB0ETYkyKffO097qXVRLA6KPmaV+/siWewR7vpfYYjWajw5KkCAwEAAQ==";
    private static final String DEFAULT_PRIVATE_KEY_STRING = "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAnijqyfhag+ZuzsNH2YZDO65RAz16tBNU+nDEjMHQRNiTIp987T3updVEsDoo+ZpX7+yJZ7BHu+l9hiNZqPDkqQIDAQABAkBgErbczRIewWFaE+GXTymUHUV01Gmu7XdXUhzy6+CZkIcEnyTpUgPilGUydiIyeiY8usvWKGjFWxLoKeJDY1wBAiEA5M9uqc9XpL5uitLWHiiq7pRxhnJb/B+wZyHqLVhCLekCIQCw9D/Fsx7vHRgymWYExHvCka7w5SyWUmNzQOOKjZUIwQIhAMqbo7JaF5GZzui+qTsrZ7C7YYtb2Hf414t7TJG6hV+BAiBXuZ7r+fL6A+h9HUNQVcAtI2AhGNxT4aBgAOlNRQd/gQIgCGqaZsOdnL9624SI1DwhBt4x24q3350pWwzgfl4Kbbo=";

    /**
     * 生成密钥对时允许的最小长度。512 位 RSA 早已可在云上廉价分解，1024 位同样不再安全。
     */
    public static final int MIN_KEY_SIZE = 2048;

    /**
     * 生成密钥对时使用的默认长度。
     */
    public static final int DEFAULT_KEY_SIZE = MIN_KEY_SIZE;

    /**
     * PKCS#1 v1.5 填充占用的字节数：单块明文上限 = 模长 - 11。
     */
    private static final int PKCS1_PADDING_SIZE = 11;

    /**
     * 512 位 RSA 的模长（字节）。Druid 原实现把块大小写死成 512 位的常量，导致 1024/2048 位密钥
     * 在分组加解密时抛 IllegalBlockSizeException / BadPaddingException。现按密钥实际模长推导块大小，
     * 该值仅作为拿不到模长时的兜底。
     */
    private static final int LEGACY_MODULUS_BYTES = 64;

    /**
     * 取密钥模长（字节），用于推导分组加解密的块大小。
     */
    private static int modulusBytes(Key key) {
        if (key instanceof RSAKey) {
            return (((RSAKey) key).getModulus().bitLength() + 7) / 8;
        }
        return LEGACY_MODULUS_BYTES;
    }

    /**
     * 解密算法 RSA
     *
     * @param cipherText - 需要解密的字符串
     * @return String - 解密后的字符串
     * @throws Exception - 解密过程中的异常信息
     */
    public static String decrypt(String cipherText) throws Exception {
        return decrypt((String) null, cipherText);
    }

    /**
     * 解密算法 RSA
     *
     * @param publicKeyText - 公钥
     * @param cipherText    - 需要解密的字符串
     * @return String - 解密后的字符串
     * @throws Exception - 解密过程中的异常信息
     */
    public static String decrypt(String publicKeyText, String cipherText) throws Exception {
        PublicKey publicKey = getPublicKey(publicKeyText);
        return decrypt(publicKey, cipherText);
    }

    /**
     * 加密算法 RSA
     *
     * @param x509File - 公钥文件
     * @return PublicKey - 加密后的字符串
     */
    public static PublicKey getPublicKeyByX509(String x509File) {
        if (x509File == null || x509File.length() == 0) {
            return getPublicKey(null);
        }

        FileInputStream in = null;
        try {
            in = new FileInputStream(x509File);
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Certificate cer = factory.generateCertificate(in);
            return cer.getPublicKey();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to get public key", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.debug("close key stream failed", e);
                }
            }
        }
    }

    /**
     * 加密算法 RSA
     *
     * @param publicKeyText - 公钥字符串
     * @return PublicKey - 公钥
     */
    public static PublicKey getPublicKey(String publicKeyText) {
        if (publicKeyText == null || publicKeyText.length() == 0) {
            publicKeyText = DEFAULT_PUBLIC_KEY_STRING;
        }

        try {
            byte[] publicKeyBytes = Base64.base64ToByteArray(publicKeyText);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyBytes);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(x509KeySpec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to get public key", e);
        }
    }

    /**
     * 加密算法RSA 通过公钥文件获取公钥
     *
     * @param publicKeyFile - 公钥文件
     * @return PublicKey - 加密后的字符串
     */
    public static PublicKey getPublicKeyByPublicKeyFile(String publicKeyFile) {
        if (publicKeyFile == null || publicKeyFile.length() == 0) {
            return getPublicKey(null);
        }

        FileInputStream in = null;
        try {
            in = new FileInputStream(publicKeyFile);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int len = 0;
            byte[] b = new byte[512 / 8];
            while ((len = in.read(b)) != -1) {
                out.write(b, 0, len);
            }

            byte[] publicKeyBytes = out.toByteArray();
            X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to get public key", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.debug("close key stream failed", e);
                }
            }
        }
    }

    /**
     * 解密算法 RSA
     *
     * @param publicKey  - 公钥
     * @param cipherText - 需要解密的字符串
     * @return String - 解密后的字符串
     * @throws Exception - 解密过程中的异常信息
     */
    public static String decrypt(PublicKey publicKey, String cipherText) throws Exception {
        // codeql[java/rsa-without-oaep]: 密文格式必须与 Druid ConfigTools 兼容，
        // 换成 OAEP 会让存量配置里的密文全部解不开；本方法的定位是配置混淆（见类注释）。
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        try {
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
        } catch (InvalidKeyException e) {
            // 因为 IBM JDK 不支持私钥加密, 公钥解密, 所以要反转公私钥
            // 也就是说对于解密, 可以通过公钥的参数伪造一个私钥对象欺骗 IBM JDK
            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
            RSAPrivateKeySpec spec = new RSAPrivateKeySpec(rsaPublicKey.getModulus(), rsaPublicKey.getPublicExponent());
            Key fakePrivateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);
            //It is a stateful object. so we need to get new one.
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, fakePrivateKey);
        }

        if (cipherText == null || cipherText.length() == 0) {
            return cipherText;
        }
        byte[] cipherBytes = Base64.base64ToByteArray(cipherText);
        int inputLength = cipherBytes.length;
        // 标识
        int offSet = 0;
        byte[] resultBytes = {};
        byte[] cache = {};
        int blockSize = modulusBytes(publicKey);
        // 分段解密
        while (inputLength - offSet > 0) {
            if (inputLength - offSet > blockSize) {
                cache = cipher.doFinal(cipherBytes, offSet, blockSize);
                offSet += blockSize;
            } else {
                cache = cipher.doFinal(cipherBytes, offSet, inputLength - offSet);
                offSet = inputLength;
            }
            resultBytes = Arrays.copyOf(resultBytes, resultBytes.length + cache.length);
            System.arraycopy(cache, 0, resultBytes, resultBytes.length - cache.length, cache.length);
        }

        return new String(resultBytes);
    }

    /**
     * 加密算法 RSA
     *
     * @param plainText - 需要加密的字符串
     * @return String - 加密后的字符串
     * @throws Exception - 加密过程中的异常信息
     */
    public static String encrypt(String plainText) throws Exception {
        return encrypt((String) null, plainText);
    }

    /**
     * 加密算法 RSA
     *
     * @param key       -  加密的密钥
     * @param plainText - 需要加密的字符串
     * @return String - 加密后的字符串
     * @throws Exception - 加密过程中的异常信息
     */
    public static String encrypt(String key, String plainText) throws Exception {
        if (key == null) {
            key = DEFAULT_PRIVATE_KEY_STRING;
        }

        byte[] keyBytes = Base64.base64ToByteArray(key);
        return encrypt(keyBytes, plainText);
    }

    /**
     * 加密算法 RSA
     *
     * @param keyBytes  - 加密的密钥
     * @param plainText - 需要加密的字符串
     * @return String - 加密后的字符串
     * @throws Exception - 加密过程中的异常信息
     */
    public static String encrypt(byte[] keyBytes, String plainText) throws Exception {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = factory.generatePrivate(spec);
        // codeql[java/rsa-without-oaep]: 与 decrypt(PublicKey, String) 成对，同样为 Druid 兼容保留 PKCS#1 v1.5。
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        try {
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        } catch (InvalidKeyException e) {
            //For IBM JDK, 原因请看解密方法中的说明
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(rsaPrivateKey.getModulus(), rsaPrivateKey.getPrivateExponent());
            Key fakePublicKey = KeyFactory.getInstance("RSA").generatePublic(publicKeySpec);
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, fakePublicKey);
        }
        byte[] inputArray = plainText.getBytes();
        int inputLength = inputArray.length;
        // 标识
        int offSet = 0;
        byte[] resultBytes = {};
        byte[] cache = {};
        int blockSize = modulusBytes(privateKey) - PKCS1_PADDING_SIZE;
        // 分段加密
        while (inputLength - offSet > 0) {
            if (inputLength - offSet > blockSize) {
                cache = cipher.doFinal(inputArray, offSet, blockSize);
                offSet += blockSize;
            } else {
                cache = cipher.doFinal(inputArray, offSet, inputLength - offSet);
                offSet = inputLength;
            }
            resultBytes = Arrays.copyOf(resultBytes, resultBytes.length + cache.length);
            System.arraycopy(cache, 0, resultBytes, resultBytes.length - cache.length, cache.length);
        }

        return Base64.byteArrayToBase64(resultBytes);
    }

    /**
     * 生成密钥对，长度取 {@link #DEFAULT_KEY_SIZE}
     *
     * @return byte[][] - 密钥对
     */
    public static byte[][] genKeyPairBytes() {
        return genKeyPairBytes(DEFAULT_KEY_SIZE);
    }

    /**
     * 生成密钥对
     *
     * @param keySize - 密钥对长度，低于 {@link #MIN_KEY_SIZE} 直接拒绝（弱密钥等同于没加密）
     * @return byte[][] - 密钥对
     */
    public static byte[][] genKeyPairBytes(int keySize) {
        if (keySize < MIN_KEY_SIZE) {
            throw new IllegalArgumentException(
                    "dynamic-datasource requires an RSA key size of at least " + MIN_KEY_SIZE + " bits, but got " + keySize);
        }
        byte[][] keyPairBytes = new byte[2][];
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(keySize, new SecureRandom());
            KeyPair pair = gen.generateKeyPair();

            keyPairBytes[0] = pair.getPrivate().getEncoded();
            keyPairBytes[1] = pair.getPublic().getEncoded();

        } catch (NoSuchAlgorithmException e) {
            // used to printStackTrace and return the half-empty array: callers silently got null
            // keys and the real failure only surfaced much later as decrypt/enlist errors.
            throw new IllegalStateException("dynamic-datasource could not generate an RSA key pair", e);
        }
        return keyPairBytes;
    }

    /**
     * 生成密钥对，长度取 {@link #DEFAULT_KEY_SIZE}
     *
     * @return String[] - Base64编码后的密钥对
     */
    public static String[] genKeyPair() {
        return genKeyPair(DEFAULT_KEY_SIZE);
    }

    /**
     * 生成密钥对
     *
     * @param keySize - 密钥对长度
     * @return String[]  - Base64编码后的密钥对
     */
    public static String[] genKeyPair(int keySize) {
        byte[][] keyPairBytes = genKeyPairBytes(keySize);
        String[] keyPairs = new String[2];
        keyPairs[0] = Base64.byteArrayToBase64(keyPairBytes[0]);
        keyPairs[1] = Base64.byteArrayToBase64(keyPairBytes[1]);
        return keyPairs;
    }

}