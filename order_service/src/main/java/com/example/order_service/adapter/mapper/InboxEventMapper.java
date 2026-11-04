package com.example.order_service.adapter.mapper;

import com.example.order_service.adapter.entity.InboxEventEntity;
import com.example.order_service.domain.model.InboxEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InboxEventMapper {
    InboxEventEntity toEntity(InboxEvent event);
}
