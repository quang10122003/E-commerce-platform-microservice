// Response chuẩn dùng chung.
package com.example.common.response;

import java.time.Instant;

import com.example.common.error.ApiErrorDto;


public record ApiResponse<T>(
	boolean success,
	String message,
	T data,
	ApiErrorDto error,
	Instant timestamp
) {
	public  static <T> ApiResponse<T> success(String message, T data){
		return new ApiResponse<T>(true,message,data,null,Instant.now());
	}
	public  static <T> ApiResponse<T> error(String message, ApiErrorDto apiError){
		return new ApiResponse<>(false,message,null,apiError,Instant.now());
	}
}
