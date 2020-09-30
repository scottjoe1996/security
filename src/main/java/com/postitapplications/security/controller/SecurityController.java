package com.postitapplications.security.controller;

import com.postitapplications.security.request.UserRequest;
import com.postitapplications.user.document.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/security")
public class SecurityController {

    private final UserRequest userRequest;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public SecurityController(UserRequest userRequest, BCryptPasswordEncoder passwordEncoder) {
        this.userRequest = userRequest;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        User userToRegister = new User(null, user.getUsername(),
            passwordEncoder.encode(user.getPassword()));
        User registeredUser = userRequest.saveUser(userToRegister);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    @PostMapping("log-in")
    public String logIn() {
        return "You are logged in!";
    }

    @DeleteMapping("log-out")
    public String logOut() {
        return "You are logged out!";
    }
}
