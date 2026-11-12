package com.example.order_service.adapter.out.openFeign;

import com.example.order_service.adapter.client.AuthUserFeignClient;
import com.example.order_service.application.dto.response.UserAvatarResponse;
import com.example.order_service.application.port.out.client.UserAvatarPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAvatarAdapter implements UserAvatarPort {
    private final AuthUserFeignClient authUserFeignClient;

    // Avatar chỉ để hiển thị nên mọi lỗi gọi auth-service (mất kết nối, timeout, 4xx/5xx) đều coi như không có avatar.
    @Override
    public Map<Long, String> findAvatarUrls(Collection<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        try {
            List<UserAvatarResponse> avatars = authUserFeignClient.getUserAvatars(List.copyOf(userIds));
            return avatars == null ? Map.of() : avatars.stream()
                    .filter((UserAvatarResponse avatar) -> avatar != null && avatar.userId() != null && avatar.avatarUrl() != null)
                    .collect(Collectors.toMap(UserAvatarResponse::userId, UserAvatarResponse::avatarUrl,
                            (String first, String second) -> first));
        } catch (RuntimeException exception) {
            log.warn("Không lấy được avatar từ auth-service, trả đánh giá không kèm avatar: users={}, error={}",
                    userIds.size(), exception.toString());
            return Map.of();
        }
    }
}
