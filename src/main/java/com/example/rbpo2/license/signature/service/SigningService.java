package com.example.rbpo2.license.signature.service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.example.rbpo2.license.signature.api.CanonicalizationApi;
import com.example.rbpo2.license.signature.api.KeyProviderApi;
import com.example.rbpo2.license.signature.api.SigningApi;
import com.example.rbpo2.license.signature.config.SignatureProperties;
import com.example.rbpo2.license.signature.error.SignatureErrorCode;
import com.example.rbpo2.license.signature.error.SignatureModuleException;
import com.example.rbpo2.license.signature.model.Base64Signature;
import com.example.rbpo2.license.signature.model.CanonicalBytes;
import com.example.rbpo2.license.signature.model.SigningKey;

@Service
public class SigningService implements SigningApi {

    private final CanonicalizationApi canonicalizationApi;
    private final KeyProviderApi keyProviderApi;
    private final SignatureProperties properties;

    public SigningService(
            CanonicalizationApi canonicalizationApi,
            KeyProviderApi keyProviderApi,
            SignatureProperties properties
    ) {
        this.canonicalizationApi = canonicalizationApi;
        this.keyProviderApi = keyProviderApi;
        this.properties = properties;
    }

    @Override
    public Base64Signature sign(Object payload) {
        try {
            CanonicalBytes canonicalBytes = canonicalizationApi.canonicalize(payload);
            SigningKey signingKey = keyProviderApi.getSigningKey();

            Signature signature = Signature.getInstance(resolveAlgorithm());
            signature.initSign(signingKey.privateKey());
            signature.update(canonicalBytes.getValue());

            return new Base64Signature(Base64.getEncoder().encodeToString(signature.sign()));
        } catch (SignatureModuleException e) {
            throw e;
        } catch (InvalidKeyException e) {
            throw new SignatureModuleException(SignatureErrorCode.KEY_FORMAT_INVALID, "signing key is invalid", e);
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureModuleException(SignatureErrorCode.SIGN_OPERATION_FAILED, "signing algorithm is not available", e);
        } catch (Exception e) {
            throw new SignatureModuleException(SignatureErrorCode.SIGN_OPERATION_FAILED, "failed to sign payload", e);
        }
    }

    private String resolveAlgorithm() {
        if (properties.getAlgorithm() == null || properties.getAlgorithm().isBlank()) {
            return "SHA256withRSA";
        }
        return properties.getAlgorithm();
    }
}