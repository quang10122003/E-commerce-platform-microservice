package com.example.order_service.application.service;

import com.example.order_service.application.port.in.CheckUserActiveOrdersUseCase;
import com.example.order_service.application.port.out.repo.CartRepoPort;
import com.example.order_service.application.port.out.repo.CheckoutRepoPort;
import com.example.order_service.application.port.out.repo.OrderRepoPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class UserAccountService implements CheckUserActiveOrdersUseCase {
    private final OrderRepoPort orderRepoPort;
    private final CheckoutRepoPort checkoutRepoPort;
    private final CartRepoPort cartRepoPort;

    // Checkout còn PROCESSING nghĩa là thanh toán/tạo đơn chưa xong nên cũng tính là đang xử lý.
    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveOrders(Long userId) {
        return orderRepoPort.existsActiveOrderOfUser(userId) || checkoutRepoPort.existsProcessingByUserId(userId);
    }

    // Chạy lại khi nhận trùng event không đổi thêm dữ liệu.
    @Transactional
    public void removeDeletedUserData(Long userId) {
        int removed = cartRepoPort.deleteByUserId(userId);
        log.info("Dọn giỏ hàng của tài khoản đã xóa: user_id={}, removed_carts={}", userId, removed);
    }
}
