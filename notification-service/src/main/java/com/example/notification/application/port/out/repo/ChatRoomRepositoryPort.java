package com.example.notification.application.port.out.repo;

import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import com.example.notification.domain.model.ChatConversation;
import com.example.notification.domain.model.ChatMessage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ChatRoomRepositoryPort {

    ChatConversation save(ChatConversation chatConversation);

    // Tải metadata room phục vụ kiểm tra quyền truy cập.
    Optional<ChatConversation> findById(Long conversationId);

    // Khóa room để các thao tác gửi đồng thời cập nhật metadata tuần tự.
    Optional<ChatConversation> findByIdForUpdate(Long conversationId);

    // Lưu tin mới và đọc lại timestamp do database sinh trong cùng transaction.
    List<ChatMessage> saveMessages(ChatConversation conversation);

    // Chỉ cập nhật thời điểm hoạt động, không ghi createdAt hoặc updatedAt.
    void updateLastMessageAt(ChatConversation conversation);

    // Tìm room theo khách hàng, shop và sản phẩm, không tạo dữ liệu.
    Optional<ChatConversation> findByCustomerIdAndShopIdAndProductId(Long customerId, Long shopId, Long productId);

    // Lấy riêng room của khách hàng hoặc shop, sắp xếp theo hoạt động mới nhất.
    PageResponse<ChatConversation> findConversationsByCustomerId(Long customerId, PageQuery pageQuery);

    PageResponse<ChatConversation> findConversationsByShopId(Long shopId, PageQuery pageQuery);

    // Đọc một tin cuối cho mỗi room trong trang, không tải toàn bộ lịch sử.
    Map<Long, ChatMessage> findLatestMessagesByConversationIds(List<Long> conversationIds);

    // Lấy lịch sử tin nhắn của đúng room theo thứ tự gửi.
    List<ChatMessage> findMessagesByConversationId(Long conversationId);

    // Đọc toàn bộ tin theo createdAt giảm dần; ID phân xử khi cùng thời gian tạo.
    List<ChatMessage> findMessagesByConversationIdNewestFirst(Long conversationId);

    // Đánh dấu tin chưa đọc của người khác và trả ID tin nhận đã đọc cuối cùng, nếu có.
    Optional<Long> markReceivedMessagesRead(Long conversationId, Long readerId);
}
