package com.example.common.untill;

public final class FileUntils {
    // check file có phải file hình ảnh
    public static void validateImage(String contentType) {
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("phải là file hình ảnh");
        }
    }

    // Lấy phần mở rộng của tên file.
    public static String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }

        return fileName.substring(fileName.lastIndexOf("."));
    }
}
