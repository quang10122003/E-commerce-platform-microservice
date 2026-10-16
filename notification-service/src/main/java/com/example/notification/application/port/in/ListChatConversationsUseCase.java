package com.example.notification.application.port.in;

import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import com.example.notification.application.dto.response.ChatConversationSummaryResponse.CustomerInfo;
import com.example.notification.application.dto.response.ChatConversationSummaryResponse.ShopInfo;
import com.example.notification.application.dto.response.ChatConversationSummaryResponse;

public interface ListChatConversationsUseCase {
    // Lấy room theo vai trò khách hàng của người đang đăng nhập.
    PageResponse<ChatConversationSummaryResponse<ShopInfo>> listCurrentCustomerConversations(PageQuery pageQuery);

    // Lấy room theo vai trò shop của người đang đăng nhập.
    PageResponse<ChatConversationSummaryResponse<CustomerInfo>> listCurrentShopConversations(PageQuery pageQuery);
}
