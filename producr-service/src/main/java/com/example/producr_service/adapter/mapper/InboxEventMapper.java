package com.example.producr_service.adapter.mapper;

import com.example.producr_service.adapter.entity.InboxEventEntity;
import com.example.producr_service.domain.model.InboxEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InboxEventMapper {
    InboxEventEntity toEntity(InboxEvent event);
}
