package com.postitapplications.authentication.repository;

import com.postitapplications.authentication.document.JwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository("MongoDbRepo")
public class JwtTokenRepository implements JwtTokenRepo {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public JwtTokenRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public JwtToken save(JwtToken jwtToken) {
        return mongoTemplate.save(jwtToken);
    }

    @Override
    public JwtToken findByToken(String token) {
        return mongoTemplate.findById(token, JwtToken.class);
    }
}
