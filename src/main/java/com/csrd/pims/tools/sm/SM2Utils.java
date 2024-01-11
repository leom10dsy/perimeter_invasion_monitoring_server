package com.csrd.pims.tools.sm;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SM2Utils {

    public static final String PUBLIC_KEY = "0409b7ee9dc67ddb75c826270a88073412eab05c023cc1dc7ffeb52d1848a043a2e2f3eddad5a7e3e2c8f70ec8d37dc9160543c82bc2a63bccc2b18d00150f15e9";
    public static final String PRIVATE_KEY = "59a28d3eab5e82b19a30e5c1fda439613bceb564098f7b2b1705d87241f61697";

    public static final int SM2_CIPHER_MODE_C1C2C3 = 0;
    public static final int SM2_CIPHER_MODE_C1C3C2 = 1;

    private static final X9ECParameters sm2ECParameters = GMNamedCurves.getByName("sm2p256v1");
    private static final ECDomainParameters domainParameters = new ECDomainParameters(
            sm2ECParameters.getCurve(),
            sm2ECParameters.getG(),
            sm2ECParameters.getN());
    private static final ECKeyPairGenerator keyPairGenerator = new ECKeyPairGenerator();

    /**
     * 生成公钥和私钥
     * @return 返回map{privateKeyHex: 私钥， publicKeyHex: 公钥}
     */
    public static Map<String, String> init() {
        try {
            keyPairGenerator.init(
                    new ECKeyGenerationParameters(domainParameters, SecureRandom.getInstance("SHA1PRNG"))
            );

            AsymmetricCipherKeyPair asymmetricCipherKeyPair = keyPairGenerator.generateKeyPair();

            // 私钥，16进制格式，自己保存
            BigInteger privatekey = ((ECPrivateKeyParameters) asymmetricCipherKeyPair.getPrivate()).getD();
            String privateKeyHex = privatekey.toString(16);

            // 公钥，16进制格式，发给前端，
            ECPoint ecPoint = ((ECPublicKeyParameters) asymmetricCipherKeyPair.getPublic()).getQ();
            String publicKeyHex = Hex.toHexString(ecPoint.getEncoded(false));

            Map<String, String> map = new HashMap<>();
            map.put("privateKeyHex", privateKeyHex);
            map.put("publicKeyHex", publicKeyHex);

            return map;
        } catch (NoSuchAlgorithmException e) {
            log.error("初始化SM2加密失败", e);
        }

        return null;

    }

    /**
     * 解密通过sm2加密的字符串
     * @param cipherData 加密后的字符串
     * @return 解密后的字符串
     */
    public static String sm2Decode(String cipherData) {
        byte[] cipherDataByte = Hex.decode(cipherData);
        BigInteger privateKeyD = new BigInteger(PRIVATE_KEY, 16);
        try {
            ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKeyD, domainParameters);
            SM2Engine sm2Engine = new SM2Engine();
            sm2Engine.init(false, privateKeyParameters);
            byte[] arrayOfBytes = sm2Engine.processBlock(cipherDataByte, SM2_CIPHER_MODE_C1C2C3, cipherDataByte.length);
            return new String(arrayOfBytes, StandardCharsets.UTF_8);
        } catch (InvalidCipherTextException e) {
            log.error(e.getMessage(), e);
        }
        return cipherData;
    }

    /**
     * 解密通过sm2加密后再进行base64加密的字符串
     * @param cipherData 加密后的字符串
     * @return 解密后的字符串
     */
    public static String sm2DecodeBase64(String cipherData) {
        byte[] cipherDataByte = Hex.decode(cipherData);
        BigInteger privateKeyD = new BigInteger(PRIVATE_KEY, 16);
        try {
            ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKeyD, domainParameters);
            SM2Engine sm2Engine = new SM2Engine();
            sm2Engine.init(false, privateKeyParameters);
            byte[] arrayOfBytes = Base64.getDecoder().decode(sm2Engine.processBlock(cipherDataByte, SM2_CIPHER_MODE_C1C2C3, cipherDataByte.length));
            return new String(arrayOfBytes, StandardCharsets.UTF_8);
        } catch (InvalidCipherTextException e) {
            log.error(e.getMessage(), e);
        }
        return cipherData;
    }

    /**
     * 通过sm2加密字符串
     * @param str 需要加密的字符串
     * @return 加密后的字符串
     */
    public static String sm2Encode(String str) {
        String publicKey = PUBLIC_KEY;
        try {
            if (publicKey.length() > 128) {
                publicKey = publicKey.substring(publicKey.length() - 128);
            }
            String stringX = publicKey.substring(0, 64);
            String stringY = publicKey.substring(64);

            BigInteger x = new BigInteger(stringX, 16);
            BigInteger y = new BigInteger(stringY, 16);

            ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(sm2ECParameters.getCurve().createPoint(x, y), domainParameters);
            SM2Engine sm2Engine = new SM2Engine();
            sm2Engine.init(true, new ParametersWithRandom(publicKeyParameters, new SecureRandom()));
            // 转为base64
            byte[] in = str.getBytes(StandardCharsets.UTF_8);
            //通过加密引擎对字节数串行加密
            byte[] arrayOfBytes = new byte[0];
            arrayOfBytes = sm2Engine.processBlock(in, SM2_CIPHER_MODE_C1C2C3, in.length);
            return Hex.toHexString(arrayOfBytes);
        } catch (InvalidCipherTextException e) {
            log.error(e.getMessage(), e);
        }

        return str;
    }

    /**
     * 通过sm2加密后再进行base64加密字符串
     * @param str 需要加密的字符串
     * @return 加密后的字符串
     */
    public static String sm2EncodeBase64(String str) {
        String publicKey = PUBLIC_KEY;
        try {
            if (publicKey.length() > 128) {
                publicKey = publicKey.substring(publicKey.length() - 128);
            }
            String stringX = publicKey.substring(0, 64);
            String stringY = publicKey.substring(64);

            BigInteger x = new BigInteger(stringX, 16);
            BigInteger y = new BigInteger(stringY, 16);

            ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(sm2ECParameters.getCurve().createPoint(x, y), domainParameters);
            SM2Engine sm2Engine = new SM2Engine();
            sm2Engine.init(true, new ParametersWithRandom(publicKeyParameters, new SecureRandom()));
            // 转为base64
            byte[] in = Base64.getEncoder().encode(str.getBytes(StandardCharsets.UTF_8));
            //通过加密引擎对字节数串行加密
            byte[] arrayOfBytes = new byte[0];
            arrayOfBytes = sm2Engine.processBlock(in, SM2_CIPHER_MODE_C1C2C3, in.length);
            return Hex.toHexString(arrayOfBytes);
        } catch (InvalidCipherTextException e) {
            log.error(e.getMessage(), e);
        }

        return str;
    }
}
