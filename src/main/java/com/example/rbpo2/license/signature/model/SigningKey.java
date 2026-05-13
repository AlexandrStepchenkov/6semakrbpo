package com.example.rbpo2.license.signature.model;

import java.security.PrivateKey;

import com.example.rbpo2.license.signature.error.SignatureErrorCode;
import com.example.rbpo2.license.signature.error.SignatureModuleException;

public record SigningKey(PrivateKey privateKey, String keyAlias) {

    public SigningKey {
        if (privateKey == null) {
            throw new SignatureModuleException(SignatureErrorCode.KEY_NOT_FOUND, "private key is required");
        }
    }
}