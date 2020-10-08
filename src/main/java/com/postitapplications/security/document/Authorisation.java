package com.postitapplications.security.document;

import com.fasterxml.jackson.annotation.JsonGetter;

public class Authorisation {
    private String authorisation;

    public Authorisation() {
    }

    public Authorisation(String authorisations) {
        this.authorisation = authorisations;
    }

    @JsonGetter("authorisation")
    public String getAuthorisation() {
        return authorisation;
    }
}
