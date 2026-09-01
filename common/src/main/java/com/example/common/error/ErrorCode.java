// interface định nghĩa ErrorCode cho các servcie triển khai theo và là đầu vào của aplication exception
package com.example.common.error;

public interface ErrorCode {
    String getCode();

    String getMessage();
}
