package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.convention.result.Results;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static com.nageoffer.shortlink.admin.common.enums.UserErrorCodeEnum.USER_NULL;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/shortlink/v1/user/{username}")
    public Result<UserRespDTO> getUserByUserName(@PathVariable("username")String username){
        UserRespDTO userByUserName = userService.getUserByUserName(username);
        if (userByUserName == null) {
            return new Result<UserRespDTO>().setCode(USER_NULL.code()).setMessage(USER_NULL.message());
        }
        return Results.success(userByUserName);
    }
}
