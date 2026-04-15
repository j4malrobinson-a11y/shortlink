package com.nageoffer.shortlink.admin.common.biz.user;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.swing.*;
import java.io.IOException;

/**
 * 用户信息传输过滤器
 *
 * @公众号：马丁玩编程，回复：加群，添加马哥微信（备注：12306）获取项目资料
 */
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpServletRequest.getRequestURI();
        if (isWhiteList(requestURI)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String token = httpServletRequest.getHeader("token");
        String userInfoJsonStr = stringRedisTemplate.opsForValue().get(token);
        if (userInfoJsonStr != null) {
            UserInfoDTO userInfoDTO = JSON.parseObject(userInfoJsonStr, UserInfoDTO.class);
            UserContext.setUser(userInfoDTO);
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }

    private boolean isWhiteList(String uri) {
        return uri.equals("/api/shortlink/v1/user/has-username")
                || uri.equals("/api/shortlink/v1/user")
                || uri.equals("/api/shortlink/v1/user/login");
    }
}