package com.baomidou.dynamic.datasource.toolkit;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
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
 */
public class CryptoUtils {

    public static final String DEFAULT_PUBLIC_KEY_STRING = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJ4o6sn4WoPmbs7DR9mGQzuuUQM9erQTVPpwxIzB0ETYkyKffO097qXVRLA6KPmaV+/siWewR7vpfYYjWajw5KkCAwEAAQ==";
    private static final String DEFAULT_PRIVATE_KEY_STRING = "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAnijqyfhag+ZuzsNH2YZDO65RAz16tBNU+nDEjMHQRNiTIp987T3updVEsDoo+ZpX7+yJZ7BHu+l9hiNZqPDkqQIDAQABAkBgErbczRIewWFaE+GXTymUHUV01Gmu7XdXUhzy6+CZkIcEnyTpUgPilGUydiIyeiY8usvWKGjFWxLoKeJDY1wBAiEA5M9uqc9XpL5uitLWHiiq7pRxhnJb/B+wZyHqLVhCLekCIQCw9D/Fsx7vHRgymWYExHvCka7w5SyWUmNzQOOKjZUIwQIhAMqbo7JaF5GZzui+qTsrZ7C7YYtb2Hf414t7TJG6hV+BAiBXuZ7r+fL6A+h9HUNQVcAtI2AhGNxT4aBgAOlNRQd/gQIgCGqaZsOdnL9624SI1DwhBt4x24q3350pWwzgfl4Kbbo=";

    /**
     * 最大加密字节数，超出最大字节数需要分组加密
     */
    private static final int MAX_ENCRYPT_BLOCK = 53;

    /**
     * 最大解密字节数，超出最大字节数需要分组解密
     */
    private static final int MAX_DECRYPT_BLOCK = 64;

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
                    e.printStackTrace();
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

            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "SunRsaSign");
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
            KeyFactory factory = KeyFactory.getInstance("RSA", "SunRsaSign");
            return factory.generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to get public key", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
        // 分段解密
        while (inputLength - offSet > 0) {
            if (inputLength - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(cipherBytes, offSet, MAX_DECRYPT_BLOCK);
                offSet += MAX_DECRYPT_BLOCK;
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
        KeyFactory factory = KeyFactory.getInstance("RSA", "SunRsaSign");
        PrivateKey privateKey = factory.generatePrivate(spec);
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
        // 分段加密
        while (inputLength - offSet > 0) {
            if (inputLength - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(inputArray, offSet, MAX_ENCRYPT_BLOCK);
                offSet += MAX_ENCRYPT_BLOCK;
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
     * 生成密钥对
     *
     * @param keySize - 密钥对长度
     * @return byte[][] - 密钥对
     */
    public static byte[][] genKeyPairBytes(int keySize) {
        byte[][] keyPairBytes = new byte[2][];
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA", "SunRsaSign");
            gen.initialize(keySize, new SecureRandom());
            KeyPair pair = gen.generateKeyPair();

            keyPairBytes[0] = pair.getPrivate().getEncoded();
            keyPairBytes[1] = pair.getPublic().getEncoded();

        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return keyPairBytes;
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