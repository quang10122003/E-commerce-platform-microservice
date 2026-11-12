package com.example.order_service.application.port.out.client;

import java.util.Collection;
import java.util.Map;

public interface UserAvatarPort {
    // URL avatar theo user id; không gọi được auth-service thì trả map rỗng thay vì ném lỗi.
    Map<Long, String> findAvatarUrls(Collection<Long> userIds);
}
