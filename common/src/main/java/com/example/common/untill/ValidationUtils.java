package com.example.common.untill;

import java.util.Locale;

import com.example.common.error.ErrorCode;
import com.example.common.exception.BusinessException;
public final class ValidationUtils {
    private ValidationUtils() {
    }

    public static void requirePositiveIfPresent(Integer value, ErrorCode errorCode) {
        if (value != null && value <= 0) {
            throw new BusinessException(errorCode);
        }
    }
    public static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String requireNormalized(String value, ErrorCode errorCode) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            throw new BusinessException(errorCode);
        }
        return normalizedValue;
    }

    public static String normalizeEmail(String email) {
        String normalizedEmail = normalize(email);
        return normalizedEmail == null ? null : normalizedEmail.toLowerCase(Locale.ROOT);
    }

    public static boolean hasText(String value) {
        return normalize(value) != null;
    }



}
