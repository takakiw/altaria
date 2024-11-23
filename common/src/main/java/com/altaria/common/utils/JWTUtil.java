package com.altaria.common.utils;

import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTUtil {

    private static final String signKey = "takaki";

    private static final Long expire = 60 * 60 * 24 * 70 * 1000L;

    public static String generateJwt(Map<String, Object> claims){
        return Jwts.builder()
                .addClaims(claims)
                .signWith(SignatureAlgorithm.HS256, signKey)
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .compact();
    }

    public static Map<String, Object> parseJwt(String jwt) {
        return Jwts.parser()
                .setSigningKey(signKey)
                .parseClaimsJws(jwt)
                .getBody();
    }



    public static String userToJWT(User dbUser) {
        Map<String, Object> mp = new HashMap<>();
        mp.put(UserConstants.USER_ID, dbUser.getId());
        mp.put(UserConstants.USER_NAME, dbUser.getUserName());
        mp.put(UserConstants.USER_EMAIL, dbUser.getEmail());
        mp.put(UserConstants.USER_NICKNAME, dbUser.getNickName());
        return JWTUtil.generateJwt(mp);
    }
}
