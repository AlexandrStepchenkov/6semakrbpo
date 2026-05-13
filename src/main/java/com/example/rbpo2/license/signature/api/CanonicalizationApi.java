package com.example.rbpo2.license.signature.api;

import com.example.rbpo2.license.signature.model.CanonicalBytes;

public interface CanonicalizationApi {

    CanonicalBytes canonicalize(Object payload);
}