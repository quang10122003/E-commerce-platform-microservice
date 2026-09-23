package com.example.producr_service.adapter.mapper;

import com.example.producr_service.adapter.entity.OutboxEventEntity;
import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.domain.model.OutboxStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

// MapperStruct chuyển đổi DTO outbox và entity persistence.
@Mapper(componentModel = "spring")
public interface OutboxEventMapper {

    // Chuyển DTO outbox thành entity để lưu xuống database.
    @Mapping(target = "id", ignore = true)
    OutboxEventEntity toEntity(OutboxEventDto event);

    // Chuyển entity persistence thành DTO để application xử lý.
    OutboxEventDto toDto(OutboxEventEntity entity);

    // Chuyển UUID thành chuỗi phù hợp với cột VARCHAR trong database.
    default String map(UUID value) {
        return value == null ? null : value.toString();
    }

    // Chuyển chuỗi từ database thành UUID của DTO.
    default UUID map(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    // Chuyển trạng thái domain sang trạng thái persistence.
    default OutboxEventEntity.Status map(OutboxStatus value) {
        return value == null ? null : OutboxEventEntity.Status.valueOf(value.name());
    }

    // Chuyển trạng thái persistence sang trạng thái domain.
    default OutboxStatus map(OutboxEventEntity.Status value) {
        return value == null ? null : OutboxStatus.valueOf(value.name());
    }
}
