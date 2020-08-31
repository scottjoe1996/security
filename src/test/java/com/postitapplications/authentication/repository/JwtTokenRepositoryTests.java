package com.postitapplications.authentication.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.postitapplications.authentication.document.JwtToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataMongoTest
@ExtendWith(SpringExtension.class)
public class JwtTokenRepositoryTests {

    @Autowired
    private MongoTemplate mongoTemplate;
    private JwtTokenRepository jwtTokenRepository;

    @BeforeEach
    public void setUp() {
        mongoTemplate.save(new JwtToken("testToken"));
        jwtTokenRepository = new JwtTokenRepository(mongoTemplate);
    }

    @AfterEach
    public void tearDown() {
        mongoTemplate.dropCollection(JwtToken.class);
    }

    @Test
    public void findByTokenShouldReturnExpectedJwtTokenWithCorrectToken() {
        String savedToken = mongoTemplate.findAll(JwtToken.class).get(0).getToken();

        JwtToken jwtTokenFound = jwtTokenRepository.findByToken(savedToken);

        assertThat(jwtTokenFound.getToken()).isEqualTo(savedToken);
    }

    @Test
    public void findByTokenShouldReturnNullWithInvalidToken() {
        assertThat(jwtTokenRepository.findByToken("noTestToken")).isEqualTo(null);
    }

    @Test
    public void findByTokenShouldThrowIllegalArgumentExceptionWhenTokenIsNull() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenRepository.findByToken(null);
        });

        assertThat(exception.getMessage()).contains("Id must not be null!");
    }

    @Test
    public void saveShouldAddAJwtTokenToTheJwtTokenDatabase() {
        jwtTokenRepository.save(new JwtToken("testToken2"));

        assertThat(mongoTemplate.findAll(JwtToken.class).size()).isEqualTo(2);
    }

    @Test
    public void saveShouldAddAPersonToThePersonDatabaseWithTheExpectedFields() {
        mongoTemplate.dropCollection(JwtToken.class);

        jwtTokenRepository.save(new JwtToken("testToken"));
        JwtToken savedJwtToken = mongoTemplate.findAll(JwtToken.class).get(0);

        assertThat(savedJwtToken.getToken()).isEqualTo("testToken");
    }
}
