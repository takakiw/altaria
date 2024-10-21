package com.altaria.common.utils;

import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTUtil {

    private static final String signKey = "takaki";

    private static final Long expire = 60 * 60 * 24 * 70 * 1000L;

    /**
     * 生成jwt
     * @param claims
     * @return
     */

    public static String generateJwt(Map<String, Object> claims){
        String jwt = Jwts.builder()
                .addClaims(claims)
                .signWith(SignatureAlgorithm.HS256, signKey)
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .compact();
        return jwt;
    }

    /**
     * 解析jwt
     * @param jwt
     * @return
     */
    public static Map<String, Object> parseJwt(String jwt) {
        Claims body = Jwts.parser()
                .setSigningKey(signKey)
                .parseClaimsJws(jwt)
                .getBody();
        return body;
    }


    /**
     * user对象转jwt
     * @param dbUser
     * @return
     */
    public static String userToJWT(User dbUser) {
        Map<String, Object> mp = new HashMap<>();
        mp.put(UserConstants.USER_ID, dbUser.getId());
        mp.put(UserConstants.USER_NAME, dbUser.getUserName());
        mp.put(UserConstants.USER_EMAIL, dbUser.getEmail());
        mp.put(UserConstants.USER_NICKNAME, dbUser.getNickName());
        String jwt = JWTUtil.generateJwt(mp);
        return jwt;
    }
}
