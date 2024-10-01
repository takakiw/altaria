package com.altaria.user.filters;


import com.alibaba.fastjson.JSONObject;
import com.altaria.common.constants.UserConstants;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.utils.JWTUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;

@WebFilter("/user/*")
public class UserFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String origin = request.getHeader("signature");
        // 判断请求是否来自gateway
        if (origin == null || !origin.equals("gateway")) {
            writerResponse(response, StatusCodeEnum.ILLEGAL_REQUEST);
            return;
        }
        // 登录和注册接口不做token验证
        String uri = request.getRequestURI();
        if (uri.contains("/login") || uri.contains("/register")) {
            filterChain.doFilter(request, response);
            return;
        }
        // 验证token
        String token = request.getHeader("Authorization");
        if (StringUtils.isEmpty(token)){
            // token为空, 未登录, 游客身份
            filterChain.doFilter(request, response);
            return;
        }
        token = token.replace("Bearer ", "");
        try {
            // 解析token, 解析成功则没有过期
            Map<String, Object> map = JWTUtil.parseJwt(token);
            String uId = map.get(UserConstants.USER_ID).toString();
            // 设置请求头
            request.setAttribute(UserConstants.USER_ID, uId);
            // 更新token
            String jwt = JWTUtil.generateJwt(map);
            response.setHeader("Authorization", "Bearer " + jwt);
        } catch (Exception e) {
            writerResponse(response, StatusCodeEnum.TOKEN_INVALID);
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
