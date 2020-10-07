package com.postitapplications.security.document;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Authorisation {
    @JsonProperty("authorisation")
    private String authorisation;

    public Authorisation() {
    }

    public Authorisation(String authorisations) {
        this.authorisation = authorisations;
    }

    public String getAuthorisation() {
        return authorisation;
    }
}
