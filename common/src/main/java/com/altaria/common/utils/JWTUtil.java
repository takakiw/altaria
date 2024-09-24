package com.altaria.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;

public class JWTUtil {

    private static final String signKey = "takaki";

    private static final Long expire = 60 * 60 * 24 * 7 * 1000L;

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
     *  解析jwt
     * @param jwt
     * @return
     */
    public static Map<String, Object> parseJwt(String jwt){
        Claims body = Jwts.parser().setSigningKey(signKey)
                .parseClaimsJwt(jwt)
                .getBody();
        return body;
    }
}
