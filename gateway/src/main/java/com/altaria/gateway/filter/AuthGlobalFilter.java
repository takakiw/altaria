package com.altaria.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.altaria.common.constants.UserConstants;

import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.utils.JWTUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Order(-1) // 保证在其他filter之前执行
public class AuthGlobalFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 验证token
        String token = request.getHeaders().getFirst("Authorization");
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
            Map<String, Object> map = null;
            try {
                map = JWTUtil.parseJwt(token);
                String uId = map.get(UserConstants.USER_ID).toString();
                ServerWebExchange webExchange = exchange.mutate().request(builder -> builder.header(UserConstants.USER_ID, uId).build()).build();
                return chain.filter(webExchange);
            } catch (Exception e) {
                // token 无效
                return handleInvalidToken(response);
            }
        }
        ServerWebExchange build = exchange.mutate().request(builder -> builder.header(UserConstants.USER_ID, "1")).build();
        return chain.filter(build);
        //return chain.filter(exchange);
    }

    private Mono<Void> handleInvalidToken(ServerHttpResponse response) {
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON); // 设置内容类型为application/json
        // 将JSON数据写入响应体
        return response.writeWith(Mono.just(response.bufferFactory().wrap(JSONObject.toJSONBytes(Result.error(StatusCodeEnum.TOKEN_INVALID)))));
    }
}
