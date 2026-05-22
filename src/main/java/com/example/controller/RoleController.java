package com.example.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RoleController {

    @GetMapping("/public/info")
    public String publicInfo() {
        return "public endpoint";
    }

    @GetMapping("/user/info")
    public String userInfo(Authentication auth) {
        return "user endpoint: " + auth.getName();
    }

    @GetMapping("/admin/info")
    public String adminInfo(Authentication auth) {
        return "admin endpoint: " + auth.getName();
    }
}
