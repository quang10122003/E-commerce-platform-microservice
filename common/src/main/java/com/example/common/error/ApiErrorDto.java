// DTO trả lỗi trong repone 
package com.example.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ApiErrorDto {
    private final String code;
    private final String message;
}
