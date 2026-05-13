package com.example.rbpo2.license.signature.model;

import java.util.Arrays;

public final class CanonicalBytes {

    private final byte[] value;

    public CanonicalBytes(byte[] value) {
        this.value = value == null ? new byte[0] : value.clone();
    }

    public byte[] getValue() {
        return value.clone();
    }

    @Override
    public String toString() {
        return "CanonicalBytes[length=" + value.length + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CanonicalBytes that)) {
            return false;
        }
        return Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}