package com.postitapplications.security.controller;

import com.postitapplications.security.document.Authorisation;
import com.postitapplications.security.request.UserRequest;
import com.postitapplications.user.document.User;
import com.postitapplications.user.utility.UserValidator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("authorisation")
    public List<Authorisation> getAuthorisations() {
        return SecurityContextHolder.getContext()
                                    .getAuthentication()
                                    .getAuthorities()
                                    .stream()
                                    .map(x -> new Authorisation(x.getAuthority()))
                                    .collect(Collectors.toList());
    }
}
