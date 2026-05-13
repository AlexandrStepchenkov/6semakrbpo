package com.example.rbpo2.license.signature;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.example.rbpo2.license.dto.Ticket;
import com.example.rbpo2.license.service.TicketSignatureService;
import com.example.rbpo2.license.signature.config.SignatureProperties;
import com.example.rbpo2.license.signature.service.JacksonCanonicalizationService;
import com.example.rbpo2.license.signature.service.KeystoreKeyProvider;
import com.example.rbpo2.license.signature.service.SigningService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class SignatureModuleTest {

    @Test
    void canonicalizationIsDeterministicRegardlessOfInsertionOrder() {
        JacksonCanonicalizationService canonicalizationService = new JacksonCanonicalizationService(objectMapper());

        Map<String, Object> first = new LinkedHashMap<>();
        first.put("b", 2);
        first.put("a", 1);

        Map<String, Object> second = new LinkedHashMap<>();
        second.put("a", 1);
        second.put("b", 2);

        assertArrayEquals(
                canonicalizationService.canonicalize(first).getValue(),
                canonicalizationService.canonicalize(second).getValue()
        );
    }

    @Test
    void ticketSignatureCanBeVerifiedWithSameModule() {
        SignatureProperties properties = new SignatureProperties();
        properties.setKeyStorePath("file:certs/signing-keystore.p12");
        properties.setKeyStoreType("PKCS12");
        properties.setKeyStorePassword("6semStepchenkovBKS2302");
        properties.setKeyAlias("ticket_signing");
        properties.setKeyPassword("6semStepchenkovBKS2302");
        properties.setAlgorithm("SHA256withRSA");

        JacksonCanonicalizationService canonicalizationService = new JacksonCanonicalizationService(objectMapper());
        KeystoreKeyProvider keyProvider = new KeystoreKeyProvider(properties);
        SigningService signingService = new SigningService(canonicalizationService, keyProvider, properties);
        TicketSignatureService ticketSignatureService = new TicketSignatureService(
                signingService,
                canonicalizationService,
                keyProvider,
                properties
        );

        Ticket ticket = new Ticket();
        ticket.setServerTime(LocalDateTime.of(2026, 5, 5, 10, 15, 30));
        ticket.setTicketLifetimeSeconds(3600);
        ticket.setFirstActivationDate(LocalDate.of(2026, 5, 5));
        ticket.setExpirationDate(LocalDate.of(2026, 6, 5));
        ticket.setUserId(42L);
        ticket.setBlocked(false);

        String signature = ticketSignatureService.signTicket(ticket);

        assertTrue(ticketSignatureService.verifyTicketSignature(ticket, signature));
    }

    private ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}