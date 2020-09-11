package com.postitapplications.authentication.security;

import com.postitapplications.authentication.document.MongoUserDetails;
import com.postitapplications.authentication.request.UserRequest;
import com.postitapplications.user.document.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class UserDetailsServiceImp implements UserDetailsService {
    private final UserRequest userRequest;

    public UserDetailsServiceImp(UserRequest userRequest) {
        this.userRequest = userRequest;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user;
        try {
            user = userRequest.getUserByUsername(username);
        } catch (HttpClientErrorException exception) {
            throw new UsernameNotFoundException(String
                .format("%s failed to authorise with error: %s", username,
                    exception.getResponseBodyAsString()));
        }

        return getUserDetails(user);
    }

    private MongoUserDetails getUserDetails(User user) {
        String[] authorities = getUserAuthorities(user);

        return new MongoUserDetails(user.getUsername(), user.getPassword(), 1, false, false, true,
            authorities);
    }

    private String[] getUserAuthorities(User user) {
        return new String[] {"ROLE_" + user.getId().toString()};
    }
}
