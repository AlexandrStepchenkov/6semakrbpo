package com.example.rbpo2.license.signature.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.rbpo2.license.signature.api.CanonicalizationApi;
import com.example.rbpo2.license.signature.error.SignatureErrorCode;
import com.example.rbpo2.license.signature.error.SignatureModuleException;
import com.example.rbpo2.license.signature.model.CanonicalBytes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
public class JacksonCanonicalizationService implements CanonicalizationApi {

    private final ObjectMapper objectMapper;

    public JacksonCanonicalizationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public CanonicalBytes canonicalize(Object payload) {
        if (payload == null) {
            throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "payload is required");
        }

        try {
            JsonNode node = payload instanceof JsonNode jsonNode ? jsonNode : objectMapper.valueToTree(payload);
            if (node == null || node.isNull() || node.isMissingNode()) {
                throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "payload is required");
            }
            String canonicalJson = render(node);
            return new CanonicalBytes(canonicalJson.getBytes(StandardCharsets.UTF_8));
        } catch (SignatureModuleException e) {
            throw e;
        } catch (Exception e) {
            throw new SignatureModuleException(SignatureErrorCode.CANONICALIZATION_ERROR, "failed to canonicalize payload", e);
        }
    }

    private String render(JsonNode node) {
        if (node.isObject()) {
            List<String> names = new ArrayList<>();
            node.fieldNames().forEachRemaining(names::add);
            Collections.sort(names);

            StringBuilder builder = new StringBuilder();
            builder.append('{');
            boolean first = true;
            for (String name : names) {
                JsonNode child = node.get(name);
                if (!first) {
                    builder.append(',');
                }
                first = false;
                builder.append(escape(name));
                builder.append(':');
                builder.append(render(child));
            }
            builder.append('}');
            return builder.toString();
        }

        if (node.isArray()) {
            StringBuilder builder = new StringBuilder();
            builder.append('[');
            for (int i = 0; i < node.size(); i++) {
                if (i > 0) {
                    builder.append(',');
                }
                builder.append(render(node.get(i)));
            }
            builder.append(']');
            return builder.toString();
        }

        if (node.isTextual()) {
            return escape(node.textValue());
        }

        if (node.isBoolean()) {
            return node.booleanValue() ? "true" : "false";
        }

        if (node.isNull()) {
            return "null";
        }

        if (node.isNumber()) {
            if (node.isFloatingPointNumber()) {
                double value = node.doubleValue();
                if (!Double.isFinite(value)) {
                    throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "non-finite numbers are not supported");
                }
                return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
            }
            return node.decimalValue().stripTrailingZeros().toPlainString();
        }

        throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "unsupported payload type: " + node.getNodeType());
    }

    private String escape(String value) {
        StringBuilder builder = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (Character.isHighSurrogate(current)) {
                if (i + 1 >= value.length() || !Character.isLowSurrogate(value.charAt(i + 1))) {
                    throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "lone surrogate characters are not supported");
                }
                builder.append(current).append(value.charAt(i + 1));
                i++;
                continue;
            }
            if (Character.isLowSurrogate(current)) {
                throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "lone surrogate characters are not supported");
            }

            switch (current) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (current <= 0x1F) {
                        builder.append(String.format("\\u%04x", (int) current));
                    } else {
                        builder.append(current);
                    }
                }
            }
        }
        return '"' + builder.toString() + '"';
    }
}