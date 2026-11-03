package com.example.order_service.order.adapter.mapper;

import com.example.order_service.order.adapter.entity.InboxEventEntity;
import com.example.order_service.order.domain.model.InboxEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InboxEventMapper {
    InboxEventEntity toEntity(InboxEvent event);
}
