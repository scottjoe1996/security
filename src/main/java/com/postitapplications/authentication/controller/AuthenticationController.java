package com.postitapplications.authentication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/security")
public class AuthenticationController {

    @GetMapping("test")
    public String testSecurity() {
        return "You are authorised!";
    }
}
