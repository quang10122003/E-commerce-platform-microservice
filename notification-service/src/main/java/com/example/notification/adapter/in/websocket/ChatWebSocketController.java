package com.example.notification.adapter.in.websocket;

import com.example.common.error.ErrorCode;
import com.example.common.error.ApiErrorDto;
import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import com.example.common.response.ApiResponse;
import com.example.notification.application.dto.request.SendChatMessageRequest;
import com.example.notification.application.port.in.SendChatMessageUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {
    private final SendChatMessageUseCase sendChatMessageUseCase;

    // Chuyển dữ liệu STOMP và danh tính CONNECT thành đầu vào thuần Java cho use case.
    @MessageMapping("/chat/rooms/{roomId}/send")
    public void sendConversationMessage(@DestinationVariable("roomId") Long roomId,
                            @Valid @Payload SendChatMessageRequest request, Principal principal) {
        if (principal == null) {
            throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
        }
        Long senderId;
        try {
            senderId = Long.parseLong(principal.getName());
        } catch (NumberFormatException exception) {
            throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
        }
        sendChatMessageUseCase.sendConversationMessage(roomId, senderId, request.content());
    }

    // Chỉ phản hồi lỗi nghiệp vụ cho session gửi tin, không phát lỗi lên topic room.
    @MessageExceptionHandler(BusinessException.class)
    @SendToUser(value = "/queue/chat/errors", broadcast = false)
    public ApiResponse<Void> handleBusinessException(BusinessException exception) {
        ErrorCode error = exception.getErrorCode();
        log.warn("Gửi tin chat bị từ chối: error_code={}", error.getCode());
        return ApiResponse.error(error.getMessage(), new ApiErrorDto(error.getCode(), error.getMessage()));
    }

    // Không đưa nội dung payload lỗi vào response hoặc log khi binding/validation thất bại.
    @MessageExceptionHandler({MethodArgumentNotValidException.class, MessageConversionException.class,
            ConversionException.class, IllegalArgumentException.class})
    @SendToUser(value = "/queue/chat/errors", broadcast = false)
    public ApiResponse<Void> handleInvalidMessage(Exception exception) {
        log.warn("Dữ liệu gửi tin chat không hợp lệ: type={}", exception.getClass().getSimpleName());
        String message = "Room hoặc nội dung tin nhắn không hợp lệ";
        return ApiResponse.error(message, new ApiErrorDto("BAD_REQUEST", message));
    }

    // Giữ chi tiết lỗi hệ thống ở backend, không lộ dữ liệu nội bộ cho client.
    @MessageExceptionHandler(Exception.class)
    @SendToUser(value = "/queue/chat/errors", broadcast = false)
    public ApiResponse<Void> handleUnexpectedException(Exception exception) {
        log.error("Gửi tin chat thất bại do lỗi hệ thống: type={}", exception.getClass().getName());
        String message = "Không thể gửi tin nhắn, vui lòng thử lại";
        return ApiResponse.error(message, new ApiErrorDto("INTERNAL_ERROR", message));
    }
}
