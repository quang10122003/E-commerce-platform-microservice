// Exception dùng ở tầng Application
package com.example.common.exception;

import com.example.common.error.ErrorCode;

import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {

	private final String codeError;

	public ApplicationException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.codeError = errorCode.getCode();
	}
}