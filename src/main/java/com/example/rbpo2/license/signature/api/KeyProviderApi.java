package com.example.rbpo2.license.signature.api;

import com.example.rbpo2.license.signature.model.SigningKey;
import com.example.rbpo2.license.signature.model.VerificationInfo;

public interface KeyProviderApi {

    SigningKey getSigningKey();

    VerificationInfo getVerificationInfo();
}