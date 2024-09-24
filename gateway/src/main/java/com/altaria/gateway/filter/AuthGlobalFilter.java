package com.altaria.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-1) // 保证在其他filter之前执行
public class AuthGlobalFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 添加请求头
        ServerWebExchange serverWebExchange = exchange.mutate().request(builder -> builder.header("Token", "your_token").build()).build();

        // 获取params参数
        String token = request.getQueryParams().getFirst("token");

        // 校验token
//        if (token == null || !token.equals("your_token")) {
//            // token校验失败，返回401
//            response.setStatusCode(HttpStatusCode.valueOf(401));
//            return response.setComplete();
//        }
        // 校验通过，继续执行下一个filter
        return chain.filter(serverWebExchange);
    }
}
