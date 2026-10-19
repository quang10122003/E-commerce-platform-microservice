package com.example.common.response;

import java.util.List;

public record ScrollResponse<T>(
        List<T> items,
        String nextCursor,
        boolean hasNext
) {
}
