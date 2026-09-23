package com.example.common.response;

import java.util.List;

// Chứa dữ liệu trả về cho API dùng cơ chế infinity scroll.
public record ScrollResponse<T>(
        List<T> items,
        String nextCursor,
        boolean hasNext
) {
}
