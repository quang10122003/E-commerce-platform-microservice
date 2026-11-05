package com.example.auth_service.adapter.out.persistence.projection;

import com.example.auth_service.domain.model.RoleEnum;

public interface UserRoleProjection {
    Long getUserId();
    RoleEnum getRoleName();
}
