package com.example.producr_service.domain.service;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;



// service domain nghiệp vụ dùng để gen mã sku cho mỗi biển thế sản phẩm
public final class SkuGenerator {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_ALPHANUMERIC_PATTERN = Pattern.compile("[^A-Z0-9]");
    private static final String RANDOM_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private SkuGenerator() {

    }

    /**
     * Sinh SKU "goc" tu ten san pham + danh sach gia tri thuoc tinh cua 1 variant.
     * Chua dam bao duy nhat toan he thong - viec do phai check qua
     * Port (ProductRepositoryPort.existsBySku) ở tấng service aplication
     */
    public static String generateBase(String productName, List<String> attributeValueTexts) {
        String namePart = toInitials(productName);

        String attributePart = attributeValueTexts.stream()
                .map(SkuGenerator::normalizeCompact)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("-"));

        return attributePart.isEmpty() ? namePart : namePart + "-" + attributePart;
    }

    /** Chuoi ngau nhien (chu hoa + so) de gan them vao SKU khi bi trung, VD "ATB-DO-S-X7K2". */
    public static String randomSuffix(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM_ALPHABET.charAt(RANDOM.nextInt(RANDOM_ALPHABET.length())));
        }
        return sb.toString();
    }

    /** Lay chu cai dau cua tung tu trong ten san pham, VD "Áo thun basic" -> "ATB". */
    private static String toInitials(String text) {
        String normalized = normalizeKeepSpaces(text);
        String[] words = normalized.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                initials.append(word.charAt(0));
            }
        }
        // truong hop ten chi co 1 tu ngan (VD "Bút") van co it nhat 1 ky tu
        return !initials.isEmpty() ? initials.toString() : "SP";
    }

    /** Bo dau tieng Viet, in hoa, GIU LAI khoang trang (dùng để tách từ). */
    private static String normalizeKeepSpaces(String text) {
        String withoutDiacritics = Normalizer.normalize(text, Normalizer.Form.NFD);
        withoutDiacritics = DIACRITICS_PATTERN.matcher(withoutDiacritics).replaceAll("");
        withoutDiacritics = withoutDiacritics.replace('Đ', 'D').replace('đ', 'd');
        return withoutDiacritics.toUpperCase();
    }

    /** Bo dau tieng Viet, in hoa, XOA khoang trang + ky tu dac biet - dùng cho từng giá trị thuộc tính */
    private static String normalizeCompact(String text) {
        String upperNoDiacritics = normalizeKeepSpaces(text);
        return NON_ALPHANUMERIC_PATTERN.matcher(upperNoDiacritics).replaceAll("");
    }
}
