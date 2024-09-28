package com.altaria.user.controller;


import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.common.Result;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user/user")
public class UserController {

    @GetMapping("/{id}")
    public Result getUserById(@PathVariable("id") Long id, @RequestHeader("Authorization") String authHeader, @RequestHeader(UserConstants.USER_ID) int userId) {
        System.out.println("Getting user by id: " + id);
        System.out.println("Authorization header: " + authHeader);
        System.out.println("User id: " + userId);
        return Result.success();
    }
}
