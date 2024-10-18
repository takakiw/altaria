package com.altaria.user.filters;


import com.alibaba.fastjson.JSONObject;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

//@WebFilter("/*")
public class UserFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String origin = request.getHeader("signature");
        // 判断请求是否来自gateway
        if (origin == null || !origin.equals("gateway")) {
            writerResponse(response, StatusCodeEnum.GATEWAY_ERROR);
            return;
        }
        filterChain.doFilter(request, response);
    }

    public void writerResponse(HttpServletResponse response, StatusCodeEnum statusCodeEnum) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(200);
        response.getWriter().write(JSONObject.toJSONString(Result.error(statusCodeEnum)));
        response.getWriter().flush();
    }
}
