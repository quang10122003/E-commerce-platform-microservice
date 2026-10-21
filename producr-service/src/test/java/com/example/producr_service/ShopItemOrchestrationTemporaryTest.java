package com.example.producr_service;

import com.example.common.exception.BusinessException;
import com.example.common.untill.JsonUtils;
import com.example.producr_service.adapter.mapper.FlashSaleItemMapper;
import com.example.producr_service.adapter.out.persistence.FlashSaleItem.FlashSaleItemJpa;
import com.example.producr_service.adapter.out.persistence.FlashSaleItem.FlashSaleItemRepoAdapter;
import com.example.producr_service.adapter.out.persistence.ProductVariant.ProductVariantJpa;
import com.example.producr_service.application.dto.response.*;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.port.out.repo.*;
import com.example.producr_service.application.service.FlashSaleItemService;
import com.example.producr_service.application.service.VariantSaleResolver;
import com.example.producr_service.domain.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ShopItemOrchestrationTemporaryTest {
    private final CurrentUserPort user = mock(CurrentUserPort.class);
    private final FlashSaleCampaignRepositoryPort campaigns = mock(FlashSaleCampaignRepositoryPort.class);
    private final FlashSaleItemRepositoryPort items = mock(FlashSaleItemRepositoryPort.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-20T00:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private final FlashSaleItemService service = new FlashSaleItemService(user, campaigns, items, clock,
            mock(ProductVariantRepoPort.class), new VariantSaleResolver(clock));

    private void prepare(FlashSaleCampaignStatus status) {
        LocalDateTime now = LocalDateTime.now(clock);
        when(user.getUserInfo()).thenReturn(UserInternaInfoRespone.builder().userId(7L).build());
        when(campaigns.findViewById(1L)).thenReturn(Optional.of(new FlashSaleCampaignView(1L, "Sale", null,
                now.minusHours(1), now.plusHours(1), now.plusHours(2), status, 1L, now, now)));
    }

    private FlashSaleItem item(Long id, Long variantId) {
        LocalDateTime now = LocalDateTime.now(clock);
        return new FlashSaleItem(id, 1L, variantId, 7L, "Snapshot deleted",
                List.of(new FlashSaleItem.Attribute("Cũ", "Đen")), BigDecimal.valueOf(20), 5, 0,
                FlashSaleItemStatus.DISABLED, now, now);
    }

    @Test
    void serviceSelectsLiveDataOrSnapshotAndFiltersDisplayName() {
        prepare(FlashSaleCampaignStatus.DRAFT);
        when(items.findShopItems(1L, 7L)).thenReturn(List.of(item(1L, 10L), item(2L, null)));
        when(items.findVariants(List.of(10L))).thenReturn(List.of(new FlashSaleVariantSnapshot(10L, 7L,
                "Current iPhone", false, BigDecimal.valueOf(301), 10,
                List.of(new FlashSaleVariantSnapshot.Attribute("Mới", "Trắng")))));
        List<ShopFlashSaleItemResponse> all = service.getShopItems(1L, null);
        assertEquals(2, all.size());
        assertEquals("Current iPhone", all.getFirst().productName());
        assertEquals("Trắng", all.getFirst().options().getFirst().value());
        assertEquals(BigDecimal.valueOf(240), all.getFirst().salePrice());
        assertFalse(all.getFirst().variantDeleted());
        assertEquals("Snapshot deleted", all.getLast().productName());
        assertEquals("Đen", all.getLast().options().getFirst().value());
        assertNull(all.getLast().salePrice());
        assertTrue(all.getLast().variantDeleted());
        assertEquals(1, service.getShopItems(1L, "  IPHONE ").size());
        assertEquals(1, service.getShopItems(1L, "SNAPSHOT").size());
        assertTrue(service.getShopItems(1L, "unknown").isEmpty());
    }

    @Test
    void missingNonNullVariantIsNotSilentlyReplacedWithSnapshot() {
        prepare(FlashSaleCampaignStatus.DRAFT);
        when(items.findShopItems(1L, 7L)).thenReturn(List.of(item(1L, 10L)));
        when(items.findVariants(List.of(10L))).thenReturn(List.of());
        assertEquals("PRODUCT_VARIANT_NOT_FOUND", assertThrows(BusinessException.class,
                () -> service.getShopItems(1L, null)).getErrorCode().getCode());
    }

    @Test
    void campaignStatusAndNotFoundStillPreventLoadingItems() {
        for (FlashSaleCampaignStatus status : List.of(FlashSaleCampaignStatus.PUBLISHED,
                FlashSaleCampaignStatus.CANCELLED, FlashSaleCampaignStatus.DONE)) {
            prepare(status);
            assertEquals(409, assertThrows(BusinessException.class,
                    () -> service.getShopItems(1L, null)).getErrorCode().getHttpStatusCode());
        }
        when(campaigns.findViewById(1L)).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(BusinessException.class,
                () -> service.getShopItems(1L, null)).getErrorCode().getHttpStatusCode());
        verifyNoInteractions(items);
    }

    @Test
    void batchLoadingUsesTwoQueriesAndDatabaseNullsDeletedVariantReference() {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(new DriverManagerDataSource("jdbc:mysql://localhost:3306/product_service_db",
                System.getenv("USERNAME_DATABASE"), System.getenv("PASSWORD_DATABASE")));
        factory.setPackagesToScan("com.example.producr_service.adapter.entity");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "none", "hibernate.generate_statistics", "true"));
        factory.afterPropertiesSet();
        EntityManagerFactory emf = factory.getObject();
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Object[] variant = (Object[]) em.createNativeQuery("SELECT v.id,v.products_id,p.name FROM product_variants v JOIN products p ON p.id=v.products_id WHERE EXISTS(SELECT 1 FROM variant_attribute_values va WHERE va.product_variants_id=v.id) ORDER BY v.id LIMIT 1").getSingleResult();
            long variantId = ((Number) variant[0]).longValue();
            em.createNativeQuery("INSERT INTO product_variants(products_id,sku,price,stock_quantity) VALUES(" + variant[1] + ",'TEMP-" + UUID.randomUUID() + "',1000,20)").executeUpdate();
            long deletedVariantId = ((Number) em.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult()).longValue();
            em.createNativeQuery("INSERT INTO flash_sale_campaigns(name,registration_deadline,starts_at,ends_at,status,created_by) VALUES('TEMP-ORCHESTRATION','2030-01-01 00:00:00','2030-01-01 01:00:00','2030-01-01 02:00:00','DRAFT',1)").executeUpdate();
            long campaignId = ((Number) em.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult()).longValue();
            em.createNativeQuery("INSERT INTO flash_sale_items(flash_sale_campaigns_id,product_variants_id,shop_id,product_name,variant_attributes,discount_percent,registered_quantity,status) VALUES(" + campaignId + "," + variantId + ",7,'Snapshot','[]',20,5,'REGISTERED'),(" + campaignId + "," + deletedVariantId + ",7,'Deleted','[]',30,5,'DISABLED')").executeUpdate();
            em.createNativeQuery("DELETE FROM product_variants WHERE id=" + deletedVariantId).executeUpdate();
            em.clear();
            JpaRepositoryFactory repositories = new JpaRepositoryFactory(em);
            FlashSaleItemJpa itemJpa = repositories.getRepository(FlashSaleItemJpa.class);
            ProductVariantJpa variantJpa = repositories.getRepository(ProductVariantJpa.class);
            FlashSaleItemRepoAdapter adapter = new FlashSaleItemRepoAdapter(variantJpa,
                    new FlashSaleItemMapper(new JsonUtils(new ObjectMapper()), null), itemJpa, null);
            SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
            sessionFactory.getStatistics().clear();
            List<FlashSaleItem> registrations = adapter.findShopItems(campaignId, 7L);
            assertEquals(2, registrations.size());
            assertEquals(variantId, registrations.getFirst().variantId());
            assertNull(registrations.getLast().variantId());
            List<FlashSaleVariantSnapshot> live = adapter.findVariants(List.of(variantId));
            assertEquals(1, live.size());
            assertEquals(variant[2], live.getFirst().productName());
            assertFalse(live.getFirst().attributes().isEmpty());
            assertEquals(2, sessionFactory.getStatistics().getPrepareStatementCount());
            assertTrue(adapter.findVariants(List.of()).isEmpty());
            assertEquals(2, sessionFactory.getStatistics().getPrepareStatementCount());
            assertTrue(adapter.findShopItems(campaignId, 8L).isEmpty());
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
            factory.destroy();
        }
    }
}
