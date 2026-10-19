package com.example.common.error;

public interface ErrorCode {
    String getCode();

    String getMessage();

    int getHttpStatusCode();
}
