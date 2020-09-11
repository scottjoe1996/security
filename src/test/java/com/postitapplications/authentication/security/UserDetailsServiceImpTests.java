package com.postitapplications.authentication.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.postitapplications.authentication.request.UserRequest;
import com.postitapplications.user.document.User;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(SpringExtension.class)
public class UserDetailsServiceImpTests {
    private UserDetailsServiceImp userDetailsServiceImp;
    @MockBean
    private UserRequest mockUserRequest;

    @Test
    public void loadUserByUsernameShouldReturnExpectedUserDetails() {
        UUID userId = UUID.randomUUID();
        when(mockUserRequest.getUserByUsername("johnSmith123"))
            .thenReturn(new User(userId, "johnSmith123", "password"));
        userDetailsServiceImp = new UserDetailsServiceImp(mockUserRequest);

        UserDetails userDetails = userDetailsServiceImp.loadUserByUsername("johnSmith123");

        assertThat(userDetails.getUsername()).isEqualTo("johnSmith123");
        assertThat(userDetails.getPassword()).isEqualTo("password");
        assertThat(userDetails.isAccountNonExpired()).isEqualTo(true);
        assertThat(userDetails.isCredentialsNonExpired()).isEqualTo(true);
        assertThat(userDetails.isAccountNonLocked()).isEqualTo(true);
        assertThat(userDetails.isEnabled()).isEqualTo(true);
        assertThat(userDetails.getAuthorities().toArray()[0].toString()).isEqualTo("ROLE_" + userId);
    }

    @Test
    public void loadUserByUsernameShouldThrowUserNotAuthorisedException() {
        when(mockUserRequest.getUserByUsername("fakeUsername123")).thenThrow(new HttpClientErrorException(
            HttpStatus.NOT_FOUND));
        userDetailsServiceImp = new UserDetailsServiceImp(mockUserRequest);

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsServiceImp.loadUserByUsername("fakeUsername123");
        });

        assertThat(exception.getMessage()).isEqualTo("fakeUsername123 failed to authorise with error: ");
    }
}
