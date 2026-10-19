package com.example.producr_service.domain.service;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;



public final class SkuGenerator {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_ALPHANUMERIC_PATTERN = Pattern.compile("[^A-Z0-9]");
    private static final String RANDOM_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private SkuGenerator() {

    }

    // SKU gốc chưa bảo đảm duy nhất; application phải kiểm tra trùng qua repository port.
    public static String generateBase(String productName, List<String> attributeValueTexts) {
        String namePart = toInitials(productName);

        String attributePart = attributeValueTexts.stream()
                .map(SkuGenerator::normalizeCompact)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("-"));

        return attributePart.isEmpty() ? namePart : namePart + "-" + attributePart;
    }

    public static String randomSuffix(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM_ALPHABET.charAt(RANDOM.nextInt(RANDOM_ALPHABET.length())));
        }
        return sb.toString();
    }

    private static String toInitials(String text) {
        String normalized = normalizeKeepSpaces(text);
        String[] words = normalized.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                initials.append(word.charAt(0));
            }
        }
        return !initials.isEmpty() ? initials.toString() : "SP";
    }

    private static String normalizeKeepSpaces(String text) {
        String withoutDiacritics = Normalizer.normalize(text, Normalizer.Form.NFD);
        withoutDiacritics = DIACRITICS_PATTERN.matcher(withoutDiacritics).replaceAll("");
        withoutDiacritics = withoutDiacritics.replace('Đ', 'D').replace('đ', 'd');
        return withoutDiacritics.toUpperCase();
    }

    private static String normalizeCompact(String text) {
        String upperNoDiacritics = normalizeKeepSpaces(text);
        return NON_ALPHANUMERIC_PATTERN.matcher(upperNoDiacritics).replaceAll("");
    }
}
