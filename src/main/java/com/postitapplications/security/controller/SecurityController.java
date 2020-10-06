package com.postitapplications.security.controller;

import com.postitapplications.security.configuration.JwtProperties;
import com.postitapplications.security.request.UserRequest;
import com.postitapplications.security.utility.JwtProvider;
import com.postitapplications.user.document.User;
import com.postitapplications.user.utility.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping("user")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        UserValidator.validateUser(user);
        User userToRegister = new User(null, user.getUsername(),
            passwordEncoder.encode(user.getPassword()));
        User registeredUser = userRequest.saveUser(userToRegister);
        return new ResponseEntity<>(registeredUser.getId().toString(), HttpStatus.CREATED);
    }

    @GetMapping("authorities")
    public ResponseEntity<GrantedAuthority[]> getAuthorities() {
        GrantedAuthority[] authorities = (GrantedAuthority[]) SecurityContextHolder.getContext()
                                                                                         .getAuthentication()
                                                                                         .getAuthorities()
                                                                                         .toArray();

        return new ResponseEntity<>(authorities, HttpStatus.OK);
    }
}
