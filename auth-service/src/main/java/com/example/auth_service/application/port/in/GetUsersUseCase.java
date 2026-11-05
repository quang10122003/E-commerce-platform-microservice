package com.example.auth_service.application.port.in;

import com.example.auth_service.domain.model.RoleEnum;

import com.example.auth_service.application.DTO.repone.UserListResponse;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;

public interface GetUsersUseCase {
    PageResponse<UserListResponse> getUsers(RoleEnum role, String keyword, Boolean locked, Boolean shopLocked, PageQuery pageQuery);
}
