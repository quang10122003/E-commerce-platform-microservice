package com.example.common.response;

import lombok.Builder;

@Builder
public record PageQuery(int page, int size) {

    public PageQuery {
        if (page < 1) {
            throw new IllegalArgumentException("page phải bắt đầu từ 1");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size phải >= 1");
        }
    }

    // Method riêng để lấy giá trị  index page cho backend vì FE gửi page đầu bằng 1
    public int pageIndex() {
        return page - 1;
    }
}
