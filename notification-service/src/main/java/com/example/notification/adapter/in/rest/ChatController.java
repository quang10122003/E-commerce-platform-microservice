package com.example.notification.adapter.in.rest;

import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import com.example.notification.application.dto.response.ChatConversationResponse;
import com.example.notification.application.dto.response.ChatMessageResponse;
import com.example.notification.application.dto.response.ReadMessgerRepone;
import com.example.notification.application.dto.response.ChatConversationSummaryResponse.CustomerInfo;
import com.example.notification.application.dto.response.ChatConversationSummaryResponse.ShopInfo;
import com.example.notification.application.dto.response.ChatConversationSummaryResponse;
import com.example.notification.application.port.in.CreateChatConversationsUseCase;
import com.example.notification.application.port.in.ListChatConversationsUseCase;
import com.example.notification.application.port.in.GetChatConversationMessagesUseCase;
import com.example.notification.application.port.in.ReadChatConversationUseCase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/chat")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class ChatController {
    CreateChatConversationsUseCase createChatConversationsUseCase;
    ListChatConversationsUseCase listChatConversationsUseCase;
    GetChatConversationMessagesUseCase getChatConversationMessagesUseCase;
    ReadChatConversationUseCase readChatConversationUseCase;

    // Chỉ nhận ID room; danh tính và quyền đọc được kiểm tra trong use case.
    @GetMapping("chat_conversations/{conversationId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getConversationMessagesNewestFirst(
            @PathVariable("conversationId") @Positive Long conversationId) {
        return ResponseEntity.ok(getChatConversationMessagesUseCase.getConversationMessagesNewestFirst(conversationId));
    }

    // Trả room bên mua và chỉ ghép thông tin shop đối thoại.
    @GetMapping("chat_conversations/user")
    public ResponseEntity<PageResponse<ChatConversationSummaryResponse<ShopInfo>>> listCurrentCustomerConversations(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return ResponseEntity.ok(listChatConversationsUseCase.listCurrentCustomerConversations(new PageQuery(page, size)));
    }

    // Trả room bên bán và chỉ ghép thông tin khách đối thoại.
    @GetMapping("chat_conversations/shop")
    public ResponseEntity<PageResponse<ChatConversationSummaryResponse<CustomerInfo>>> listCurrentShopConversations(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return ResponseEntity.ok(listChatConversationsUseCase.listCurrentShopConversations(new PageQuery(page, size)));
    }

    // Lấy ID room hiện có hoặc tạo mới theo sản phẩm và người đang đăng nhập.
    @PostMapping("chat_conversations/{productId}")
    public ResponseEntity<ChatConversationResponse> getOrCreateConversationByProductId(
            @PathVariable("productId") Long productId) {
        return ResponseEntity.ok(createChatConversationsUseCase.getOrCreateConversationByProductId(productId));
    }
    // Đánh dấu tin đã đọc; không nhận danh tính người đọc từ client.
    @PutMapping("conversations/{conversationId}/read")
    public ResponseEntity<ReadMessgerRepone> readConversationMessages(
            @PathVariable("conversationId") @Positive Long conversationId) {
        return ResponseEntity.ok(readChatConversationUseCase.readConversationMessages(conversationId));
    }
}
