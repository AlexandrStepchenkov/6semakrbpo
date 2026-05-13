package com.example.rbpo2.license.signature.model;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

import com.example.rbpo2.license.signature.error.SignatureErrorCode;
import com.example.rbpo2.license.signature.error.SignatureModuleException;

public record VerificationInfo(PublicKey publicKey, X509Certificate certificate) {

    public VerificationInfo {
        if (publicKey == null) {
            throw new SignatureModuleException(SignatureErrorCode.KEY_NOT_FOUND, "public key is required");
        }
    }
}