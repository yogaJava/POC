package com.xycm.poc.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AKSKGenerator {
    public static String generateAK() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder ak = new StringBuilder(18);
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 18; ++i) {
            ak.append(chars.charAt(random.nextInt(chars.length())));
        }

        return ak.toString();
    }

    public static String generateSK() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256, new SecureRandom());
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded()).substring(0, 32);
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        String ak = generateAK();
        String sk = generateSK();
        SignUtil.encryptData(ak, sk);
        String decryptedSk = SignUtil.decryptData("kcg5aMzfF92wkYgVuc", "Df0z7uIVJt+Dti823OCxeO0GoZcakJ998ht+OLdEVsm8B3M6kaofrcHKOkF4Kuj/Qawse");
        System.out.println("解密的SK：" + decryptedSk);
    }
}
