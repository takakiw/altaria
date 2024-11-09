package com.altaria.common.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SignUtil {


    private static final String secretKey = "takaki";

    public static String sign(long uid, long id, long expire){
        try {
            String data = uid + ":" + id  + ":" + expire;
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSHA256.init(secretKeySpec);
            byte[] hash = hmacSHA256.doFinal(data.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean checkSign(long uid, long id, long expire, String sign){
        try{
            if(Long.compare(expire, System.currentTimeMillis()) != -1){
                String newSign = sign(uid, id, expire);
                return newSign.equals(sign);
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }

}
