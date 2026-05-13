package com.example.rbpo2.license.signature.model;

import com.example.rbpo2.license.signature.error.SignatureErrorCode;
import com.example.rbpo2.license.signature.error.SignatureModuleException;

public record Base64Signature(String value) {

    public Base64Signature {
        if (value == null || value.isBlank()) {
            throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "signature value is required");
        }
    }
}