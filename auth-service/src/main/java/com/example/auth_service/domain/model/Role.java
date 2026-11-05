package com.example.auth_service.domain.model;

import com.example.auth_service.domain.model.RoleEnum;

public class Role {

    private Long id;
    private RoleEnum name;

    public Role(
            Long id,
            RoleEnum name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public RoleEnum getName() {
        return name;
    }
    
}
