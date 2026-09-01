// DTO trả lỗi trong repone 
package com.example.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ApiErrorDto {
    String code;
    String message;
}
