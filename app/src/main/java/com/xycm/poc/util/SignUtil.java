package com.xycm.poc.util;


import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.SmUtil;

public class SignUtil {

    public static Map<String, Object> buildParamMap(String appSecret, String appKey, String dataJsonStr) {
        String timestamp = System.currentTimeMillis() + "";
        String nonce = IdUtil.fastSimpleUUID();
        String data = encryptData(appSecret, dataJsonStr);
        String sign = SecureUtil.hmacSha256(appSecret).digestHex(StrUtil.format("apiKey={}&data={}&nonce={}&ts={}", new Object[]{appKey, data, nonce, timestamp})).toUpperCase();
        Map<String, Object> paramMap = new HashMap<>(5);
        paramMap.put("apiKey", appKey);
        paramMap.put("ts", timestamp);
        paramMap.put("nonce", nonce);
        paramMap.put("sign", sign);
        paramMap.put("data", data);
        return paramMap;
    }

    public static String encryptData(String appSecret, String data) {
        return SmUtil.sm4(appSecret.substring(0, 16).getBytes()).encryptBase64(data) + AKSKGenerator.generateAK().substring(0, 5);
    }

    public static String encryptDataHex(String appSecret, String data) {
        return SmUtil.sm4(appSecret.substring(0, 16).getBytes()).encryptHex(data) + AKSKGenerator.generateAK().substring(0, 5);
    }

    public static String decryptData(String appSecret, String data) {
        try {
            data = data.substring(0, data.length() - 5);
            return SmUtil.sm4(appSecret.substring(0, 16).getBytes()).decryptStr(data);
        } catch (Exception e) {
            Log.e("解密失败！", Objects.requireNonNull(e.getMessage()));
        }
        return "";
    }

    public static void main(String[] args) {
        String appKey = "s4wxTdme4R7KSgEE8b";
        String appSecret = "WzxjrhnVKPL+L26T1uWMIlzNk2D/6N18";
        appKey = "kcg5aMzfF92wkYgVuc";
        appSecret = "g48MGlraupdnbdmLN0jv4wD8SDtujd87";
        System.out.println(decryptData(appSecret, "8d85efc629c06c7752c1e60550caa7dbq46Ub"));
        System.out.println(encryptDataHex(appSecret, "12345678"));
    }

}
