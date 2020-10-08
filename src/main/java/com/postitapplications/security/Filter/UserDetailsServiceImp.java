package com.postitapplications.security.Filter;

import com.postitapplications.exception.exceptions.ExternalServiceException;
import com.postitapplications.security.document.SecurityUserDetails;
import com.postitapplications.security.request.UserRequest;
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
        } catch (ExternalServiceException exception) {
            throw new UsernameNotFoundException(String
                .format("%s failed to authorise with error: %s", username,
                    exception.getMessage()));
        }

        return getUserDetails(user);
    }

    private SecurityUserDetails getUserDetails(User user) {
        String[] authorities = getUserAuthorities(user);

        return new SecurityUserDetails(user.getUsername(), user.getPassword(), authorities);
    }

    private String[] getUserAuthorities(User user) {
        return new String[] {"ROLE_" + user.getId().toString()};
    }
}
