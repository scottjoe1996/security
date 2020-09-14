package com.postitapplications.security.configuration;

import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "services")
public class ExternalServiceProperties {
    @NotNull
    private String userUrl;

    public void setUserUrl(String userUrl) {
        this.userUrl = userUrl;
    }

    public String getUserUrl() {
        return userUrl;
    }
}
