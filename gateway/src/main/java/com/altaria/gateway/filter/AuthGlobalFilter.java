package com.altaria.gateway.filter;

import com.altaria.common.constants.UserConstants;
import com.altaria.common.utils.BaseContext;
import com.altaria.common.utils.JWTUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Constants;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

//@Component
//@Order(-1) // 保证在其他filter之前执行
public class AuthGlobalFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String path = request.getURI().getPath();// 获取请求路径
        if (path.contains("login") || path.contains("register")){
            return chain.filter(exchange);
        }
        // 验证token
        String token = request.getHeaders().getFirst("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatusCode(HttpStatusCode.valueOf(401));
            return response.setComplete();
        }
        token = token.substring(7);
        Map<String, Object> map = JWTUtil.parseJwt(token);
        String uId = map.get(UserConstants.USER_ID).toString();
        ServerWebExchange webExchange = exchange.mutate().request(builder -> builder.header(UserConstants.USER_ID, uId).build()).build();
        return chain.filter(webExchange);
    }
}
