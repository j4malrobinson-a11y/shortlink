package com.nageoffer.shortlink.admin.common.biz.user;

import com.alibaba.fastjson2.JSON;
import com.nageoffer.shortlink.admin.common.convention.exception.ClientException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class UserInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String token = request.getHeader("token");
        if (token == null) {
            throw new ClientException("用户未登录");
        }

        String userInfoJsonStr = stringRedisTemplate.opsForValue().get(token);

        if (userInfoJsonStr == null) {
            throw new ClientException("用户未登录");
        }

        UserInfoDTO userInfoDTO = JSON.parseObject(userInfoJsonStr, UserInfoDTO.class);
        UserContext.setUser(userInfoDTO);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.removeUser();
    }
}