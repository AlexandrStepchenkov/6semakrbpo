package com.example.rbpo2.license.signature.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.example.rbpo2.license.signature.api.KeyProviderApi;
import com.example.rbpo2.license.signature.config.SignatureProperties;
import com.example.rbpo2.license.signature.error.SignatureErrorCode;
import com.example.rbpo2.license.signature.error.SignatureModuleException;
import com.example.rbpo2.license.signature.model.SigningKey;
import com.example.rbpo2.license.signature.model.VerificationInfo;

@Service
public class KeystoreKeyProvider implements KeyProviderApi {

    private final SignatureProperties properties;
    private volatile KeyBundle cachedBundle;

    public KeystoreKeyProvider(SignatureProperties properties) {
        this.properties = properties;
    }

    @Override
    public SigningKey getSigningKey() {
        return loadBundle().signingKey();
    }

    @Override
    public VerificationInfo getVerificationInfo() {
        return loadBundle().verificationInfo();
    }

    private KeyBundle loadBundle() {
        KeyBundle bundle = cachedBundle;
        if (bundle != null) {
            return bundle;
        }

        synchronized (this) {
            if (cachedBundle != null) {
                return cachedBundle;
            }
            cachedBundle = loadBundleFromSource();
            return cachedBundle;
        }
    }

    private KeyBundle loadBundleFromSource() {
        validateConfiguration();

        try {
            KeyStore keyStore = KeyStore.getInstance(resolveKeyStoreType());
            try (InputStream inputStream = openKeyStoreStream(resolveKeyStorePath())) {
                keyStore.load(inputStream, resolveStorePassword().toCharArray());
            }

            String alias = properties.getKeyAlias();
            if (alias == null || alias.isBlank() || !keyStore.containsAlias(alias)) {
                throw new SignatureModuleException(SignatureErrorCode.KEY_NOT_FOUND, "key alias not found: " + alias);
            }

            Key key = keyStore.getKey(alias, resolveKeyPassword().toCharArray());
            if (!(key instanceof PrivateKey privateKey)) {
                throw new SignatureModuleException(SignatureErrorCode.KEY_FORMAT_INVALID, "alias does not contain a private key: " + alias);
            }

            VerificationInfo verificationInfo = buildVerificationInfo(keyStore, alias);
            return new KeyBundle(new SigningKey(privateKey, alias), verificationInfo);
        } catch (SignatureModuleException e) {
            throw e;
        } catch (Exception e) {
            throw classifyLoadFailure(e);
        }
    }

    private VerificationInfo buildVerificationInfo(KeyStore keyStore, String alias) throws Exception {
        X509Certificate storeCertificate = null;
        Certificate certificate = keyStore.getCertificate(alias);
        if (certificate instanceof X509Certificate x509Certificate) {
            storeCertificate = x509Certificate;
        }

        X509Certificate configuredCertificate = parseConfiguredCertificate();
        X509Certificate effectiveCertificate = configuredCertificate != null ? configuredCertificate : storeCertificate;

        if (effectiveCertificate == null) {
            throw new SignatureModuleException(SignatureErrorCode.KEY_NOT_FOUND, "public certificate not found for alias: " + alias);
        }

        if (storeCertificate != null && configuredCertificate != null &&
                !Arrays.equals(storeCertificate.getPublicKey().getEncoded(), configuredCertificate.getPublicKey().getEncoded())) {
            throw new SignatureModuleException(SignatureErrorCode.KEY_FORMAT_INVALID, "configured public certificate does not match keystore certificate");
        }

        PublicKey publicKey = effectiveCertificate.getPublicKey();
        if (publicKey == null) {
            throw new SignatureModuleException(SignatureErrorCode.KEY_FORMAT_INVALID, "public key is not available");
        }

        return new VerificationInfo(publicKey, effectiveCertificate);
    }

    private X509Certificate parseConfiguredCertificate() {
        String configuredCertificate = properties.getPublicCertificateBase64();
        if (configuredCertificate == null || configuredCertificate.isBlank()) {
            return null;
        }

        try {
            byte[] certificateBytes = Base64.getMimeDecoder().decode(stripPemDecorations(configuredCertificate));
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Certificate certificate = certificateFactory.generateCertificate(new ByteArrayInputStream(certificateBytes));
            if (certificate instanceof X509Certificate x509Certificate) {
                return x509Certificate;
            }
            throw new SignatureModuleException(SignatureErrorCode.KEY_FORMAT_INVALID, "configured certificate is not X.509");
        } catch (SignatureModuleException e) {
            throw e;
        } catch (Exception e) {
            throw new SignatureModuleException(SignatureErrorCode.KEY_FORMAT_INVALID, "failed to parse configured certificate", e);
        }
    }

    private String stripPemDecorations(String value) {
        return value
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll("\\s", "");
    }

private InputStream openKeyStoreStream(String path) throws IOException {

    if (path.startsWith("classpath:")) {
        String resourcePath = path.substring("classpath:".length());
        InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourcePath);

        if (is != null) {
            return is;
        }

        Path fallbackPath = Path.of("certs", Path.of(resourcePath).getFileName().toString());
        if (Files.exists(fallbackPath)) {
            return Files.newInputStream(fallbackPath);
        }

        throw new IOException("Classpath resource not found: " + resourcePath);
    }

    if (path.startsWith("file:")) {
        return Files.newInputStream(Path.of(path.substring("file:".length())));
    }

    InputStream is = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream(path);

    if (is != null) return is;

    return Files.newInputStream(Path.of(path));
}

    private String resolveKeyStorePath() {
        if (properties.getKeyStorePath() == null || properties.getKeyStorePath().isBlank()) {
            return "classpath:signing-keystore.p12";
        }
        return properties.getKeyStorePath();
    }

    private String resolveKeyStoreType() {
        if (properties.getKeyStoreType() == null || properties.getKeyStoreType().isBlank()) {
            return "PKCS12";
        }
        return properties.getKeyStoreType();
    }

    private String resolveStorePassword() {
        if (properties.getKeyStorePassword() == null || properties.getKeyStorePassword().isBlank()) {
            throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "keystore password is required");
        }
        return properties.getKeyStorePassword();
    }

    private String resolveKeyPassword() {
        if (properties.getKeyPassword() == null || properties.getKeyPassword().isBlank()) {
            return resolveStorePassword();
        }
        return properties.getKeyPassword();
    }

    private void validateConfiguration() {
        if (properties.getKeyAlias() == null || properties.getKeyAlias().isBlank()) {
            throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "key alias is required");
        }
    }

    private SignatureModuleException classifyLoadFailure(Exception exception) {
        String message = exception.getMessage() == null ? "" : exception.getMessage().toLowerCase();
        if (message.contains("password") || message.contains("integrity check failed") || message.contains("decrypt")) {
            return new SignatureModuleException(SignatureErrorCode.AUTH_FAILED, "failed to unlock keystore or private key", exception);
        }
        if (message.contains("not found") || message.contains("no such file") || message.contains("cannot find") || message.contains("does not exist")) {
            return new SignatureModuleException(SignatureErrorCode.KEY_SOURCE_UNAVAILABLE, "keystore source is unavailable", exception);
        }
        if (message.contains("algorithm") || message.contains("certificate") || message.contains("key format")) {
            return new SignatureModuleException(SignatureErrorCode.KEY_FORMAT_INVALID, "keystore format is invalid", exception);
        }
        return new SignatureModuleException(SignatureErrorCode.KEY_PROVIDER_ERROR, "failed to load signing key material", exception);
    }

    private record KeyBundle(SigningKey signingKey, VerificationInfo verificationInfo) {
    }
}