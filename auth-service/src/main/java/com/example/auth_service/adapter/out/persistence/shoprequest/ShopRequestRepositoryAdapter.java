package com.example.auth_service.adapter.out.persistence.shoprequest;

import com.example.auth_service.adapter.mapper.ShopRequestMapper;
import com.example.auth_service.application.DTO.repone.AdminShopRequestResponse;
import com.example.auth_service.application.port.out.ShopRequestRepositoryPort;
import com.example.auth_service.domain.model.ShopRequest;
import com.example.auth_service.domain.model.ShopRequestStatus;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShopRequestRepositoryAdapter implements ShopRequestRepositoryPort {
    private final ShopRequestRepoJpa shopRequestRepoJpa;
    private final ShopRequestMapper shopRequestMapper;

    @Override
    public ShopRequest save(ShopRequest shopRequest) {
        return shopRequestMapper.toDomain(shopRequestRepoJpa.save(shopRequestMapper.toEntity(shopRequest)));
    }

    @Override
    public Optional<ShopRequest> findLatestByUserId(Long userId) {
        return shopRequestRepoJpa.findFirstByUserIdOrderByIdDesc(userId).map(shopRequestMapper::toDomain);
    }

    @Override
    public Optional<ShopRequest> findByIdAndUserId(Long id, Long userId) {
        return shopRequestRepoJpa.findByIdAndUserId(id, userId).map(shopRequestMapper::toDomain);
    }

    @Override
    public boolean existsPendingByUserId(Long userId) {
        return shopRequestRepoJpa.existsByUserIdAndStatus(userId, ShopRequestStatus.PENDING);
    }

    @Override
    public Optional<ShopRequest> findById(Long id) {
        return shopRequestRepoJpa.findById(id).map(shopRequestMapper::toDomain);
    }

    @Override
    public Optional<ShopRequest> findByIdForUpdate(Long id) {
        return shopRequestRepoJpa.findByIdForUpdate(id).map(shopRequestMapper::toDomain);
    }

    @Override
    public PageResponse<AdminShopRequestResponse> findAdminPage(ShopRequestStatus status, String keyword,
                                                                PageQuery pageQuery, ZoneId zoneId) {
        String pattern = keyword == null ? null : "%" + keyword.toLowerCase(Locale.ROOT)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        Page<AdminShopRequestResponse> page = shopRequestRepoJpa.findAdminPage(status, pattern,
                        PageRequest.of(pageQuery.pageIndex(), pageQuery.size()))
                .map(projection -> shopRequestMapper.toAdminResponse(projection, zoneId));
        List<AdminShopRequestResponse> items = page.getContent();
        return new PageResponse<>(items, pageQuery.page(), pageQuery.size(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public Optional<AdminShopRequestResponse> findAdminView(Long id, ZoneId zoneId) {
        return shopRequestRepoJpa.findAdminView(id).map(projection -> shopRequestMapper.toAdminResponse(projection, zoneId));
    }
}
