package com.postitapplications.authentication.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.postitapplications.authentication.document.MongoUserDetails;
import com.postitapplications.authentication.request.UserRequest;
import com.postitapplications.user.document.User;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class UserDetailsProviderTests {
    private UserDetailsProvider userDetailsProvider;
    @MockBean
    private UserRequest mockUserRequest;

    @Test
    public void loadUserByUsernameShouldReturnExpectedUserDetails() {
        when(mockUserRequest.getUserByUsername("johnSmith123"))
            .thenReturn(new User(UUID.randomUUID(), "johnSmith123", "password"));
        userDetailsProvider = new UserDetailsProvider(mockUserRequest);

        UserDetails userDetails = userDetailsProvider.loadUserByUsername("johnSmith123");

        assertThat(userDetails.getUsername()).isEqualTo("johnSmith123");
    }
}
