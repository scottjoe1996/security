package com.postitapplications.authentication.security;

import com.postitapplications.authentication.document.MongoUserDetails;
import com.postitapplications.authentication.request.UserRequest;
import com.postitapplications.exception.exceptions.UserNotFoundException;
import com.postitapplications.user.document.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsProvider implements UserDetailsService {
    private final UserRequest userRequest;

    public UserDetailsProvider(UserRequest userRequest) {
        this.userRequest = userRequest;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRequest.getUserByUsername(username);
        if (user == null) {
            throw new UserNotFoundException(
                String.format("User with username %s was not found", username));
        }

        String[] authorities = getUserAuthorities(user);

        return new MongoUserDetails(user.getUsername(),
            user.getPassword(), 1, false, false, true, authorities);
    }

    private String[] getUserAuthorities(User user) {
        return new String[] {user.getId().toString()};
    }
}
