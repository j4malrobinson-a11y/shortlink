package com.nageoffer.shortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.convention.result.Results;
import com.nageoffer.shortlink.admin.dto.req.UserLoginReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserActualRespDTO;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    @GetMapping("/api/shortlink/v1/user/{username}")
    public Result<UserRespDTO> getUserByUserName(@PathVariable("username")String username){
        UserRespDTO userByUserName = userService.getUserByUserName(username);
        return Results.success(userByUserName);
    }

    @GetMapping("/api/shortlink/v1/actual/user/{username}")
    public Result<UserActualRespDTO> getUserActualByUserName(@PathVariable("username")String username){
        UserRespDTO userByUserName = userService.getUserByUserName(username);
        return Results.success(BeanUtil.toBean(userByUserName,UserActualRespDTO.class));
    }

    @GetMapping("/api/shortlink/v1/user/has-username")
    public Boolean hasUsername(@RequestParam("username")String username){
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @PostMapping("/api/shortlink/v1/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO requestParam){
        userService.register(requestParam);
        return Results.success();
    }

    @PutMapping("/api/shortlink/v1/user")
    public Result<Void> update(@RequestBody UserUpdateReqDTO requestRaram){
        userService.update(requestRaram);
        return Results.success();
    }

    @PostMapping("/api/shortlink/v1/user/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestparam){
        UserLoginRespDTO userLoginRespDTO = userService.login(requestparam);
        return Results.success(userLoginRespDTO);
    }

    @GetMapping("/api/shortlink/v1/user/checklogin")
    public Result<Boolean> checkLogin(@RequestParam("token") String token){
        return Results.success(userService.checkLogin(token));
    }

    @DeleteMapping("/api/shortlink/v1/user/logout")
    public Result<Void> logout(@RequestParam("token") String token,@RequestParam("username") String username){
        userService.logout(token,username);
        return Results.success();
    }
}
