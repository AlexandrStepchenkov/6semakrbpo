package com.example.rbpo2.license.signature.api;

import com.example.rbpo2.license.signature.model.Base64Signature;

public interface SigningApi {

    Base64Signature sign(Object payload);
}