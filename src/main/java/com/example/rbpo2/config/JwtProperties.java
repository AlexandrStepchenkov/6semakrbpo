package com.example.rbpo2.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long accessExpiration = 900000; // 15 minutes by default
    private long refreshExpirationDays = 7; // 7 days by default

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessExpiration() {
        return accessExpiration > 0 ? accessExpiration : 900000; // Ensure minimum 15 min
    }

    public void setAccessExpiration(long accessExpiration) {
        this.accessExpiration = accessExpiration;
    }

    public long getRefreshExpirationDays() {
        return refreshExpirationDays > 0 ? refreshExpirationDays : 7;
    }

    public void setRefreshExpirationDays(long refreshExpirationDays) {
        this.refreshExpirationDays = refreshExpirationDays;
    }
}