package com.postitapplications.authentication.service;

import com.postitapplications.authentication.document.JwtToken;
import com.postitapplications.authentication.repository.JwtTokenRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final JwtTokenRepo jwtTokenRepo;

    @Autowired
    public JwtTokenService(@Qualifier("MongoDbRepo") JwtTokenRepo jwtTokenRepo) {
        this.jwtTokenRepo = jwtTokenRepo;
    }

    public JwtToken saveJwtToken(JwtToken jwtToken) {
        return jwtTokenRepo.save(jwtToken);
    }

    public JwtToken getJwtTokenByToken(String token) {
        return jwtTokenRepo.findByToken(token);
    }
}
