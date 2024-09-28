package com.altaria.user.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/user")
public class UserController {

    @GetMapping("/{id}")
    public String getUserById(Long id) {
        return "User with id " + id;
    }
}
