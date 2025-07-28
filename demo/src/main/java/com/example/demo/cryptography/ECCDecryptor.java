//package com.example.demo.cryptography;
//
//import java.security.KeyPairGenerator;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import java.security.Security;
//
//public class ECCDecryptor {
//    private static final String ECC_CURVE = "secp256r1";  // Кривая NIST P-256 (стандартная)
//    private static final String AES_ALG = "AES/GCM/NoPadding";  // Режим AES-GCM
//    private static final int GCM_TAG_LENGTH = 128;  // Длина тега аутентификации (16 байт)
//
//    static {
//        Security.addProvider(new BouncyCastleProvider());  // Регистрация BouncyCastle
//    }
//
//    public static String decryptWithPrivateKey(String encryptedDataBase64, String privateKeyBase64) throws Exception {
//
//        return new String(decrypted);
//    }
//}
