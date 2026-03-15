package com.nageoffer.shortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.convention.result.Results;
import com.nageoffer.shortlink.admin.dto.resp.UserActualRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/shortlink/v1/user/{username}")
    public Result<UserRespDTO> getUserByUserName(@PathVariable("username")String username){
        UserRespDTO userByUserName = userService.getUserByUserName(username);
        return Results.success(userByUserName);
    }

    @GetMapping("/api/shortlink/v1/user/{username}")
    public Result<UserActualRespDTO> getUserActualByUserName(@PathVariable("username")String username){
        UserRespDTO userByUserName = userService.getUserByUserName(username);
        return Results.success(BeanUtil.toBean(userByUserName,UserActualRespDTO.class));
    }
}
