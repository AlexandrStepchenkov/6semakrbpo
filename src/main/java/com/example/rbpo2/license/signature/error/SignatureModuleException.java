package com.example.rbpo2.license.signature.error;

public class SignatureModuleException extends RuntimeException {

    private final SignatureErrorCode errorCode;

    public SignatureModuleException(SignatureErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SignatureModuleException(SignatureErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public SignatureErrorCode getErrorCode() {
        return errorCode;
    }
}