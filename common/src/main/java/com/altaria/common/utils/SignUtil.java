package com.altaria.common.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SignUtil {


    private static final String secretKey = "takaki";

    public static String sign(long uid, String url, long expire){
        try {
            String data = uid + ":" + url  + ":" + expire;
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSHA256.init(secretKeySpec);
            byte[] hash = hmacSHA256.doFinal(data.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean checkSign(long uid, String url, long expire, String sign){
        try{
            if(Long.compare(expire, System.currentTimeMillis()) != -1){
                String newSign = sign(uid, url, expire);
                return newSign.equals(sign);
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }

}
