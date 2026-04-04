package com.example.carsharing.license.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TicketSignatureService {

    private final String keystorePath;
    private final String keystorePassword;
    private final String keyAlias;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public TicketSignatureService(
            @Value("${license.keystore.path:classpath:keystore.p12}") String keystorePath,
            @Value("${license.keystore.password:6semStepchenkovBKS2302}") String keystorePassword,
            @Value("${license.keystore.key-alias:laba6_key}") String keyAlias) {
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.keyAlias = keyAlias;
    }

    private String normalizeClasspathPath(String path) {
        if (path == null) {
            return "keystore.p12";
        }
        return path.startsWith("classpath:") ? path.substring("classpath:".length()) : path;
    }

    public String signTicket(Object ticket) {
        try {
            String ticketJson = objectMapper.writeValueAsString(ticket);
            byte[] ticketBytes = ticketJson.getBytes(StandardCharsets.UTF_8);

            PrivateKey privateKey = loadPrivateKey();

            Signature signature = Signature.getInstance("SHA256WithRSA");
            signature.initSign(privateKey);
            signature.update(ticketBytes);
            byte[] signatureBytes = signature.sign();

            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("Error signing ticket", e);
            throw new RuntimeException("Failed to sign ticket", e);
        }
    }

    public boolean verifyTicketSignature(Object ticket, String signatureBase64) {
        try {
            String ticketJson = objectMapper.writeValueAsString(ticket);
            byte[] ticketBytes = ticketJson.getBytes(StandardCharsets.UTF_8);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

            PublicKey publicKey = loadPublicKeyFromCertificate();

            Signature signature = Signature.getInstance("SHA256WithRSA");
            signature.initVerify(publicKey);
            signature.update(ticketBytes);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            log.error("Error verifying ticket signature", e);
            return false;
        }
    }

    private PrivateKey loadPrivateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        ClassPathResource resource = new ClassPathResource(normalizeClasspathPath(keystorePath));
        try (InputStream fis = resource.getInputStream()) {
            keyStore.load(fis, keystorePassword.toCharArray());
        }
        return (PrivateKey) keyStore.getKey(keyAlias, keystorePassword.toCharArray());
    }

    private PublicKey loadPublicKeyFromCertificate() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        ClassPathResource resource = new ClassPathResource(normalizeClasspathPath(keystorePath));
        try (InputStream fis = resource.getInputStream()) {
            keyStore.load(fis, keystorePassword.toCharArray());
        }
        return keyStore.getCertificate(keyAlias).getPublicKey();
    }
}
