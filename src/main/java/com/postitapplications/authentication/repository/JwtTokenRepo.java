package com.postitapplications.authentication.repository;

import com.postitapplications.authentication.document.JwtToken;

public interface JwtTokenRepo {
    JwtToken save(JwtToken jwtToken);

    JwtToken findByToken(String token);
}
