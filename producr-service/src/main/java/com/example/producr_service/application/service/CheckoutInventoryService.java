package com.example.producr_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.application.dto.checkout.CheckoutItemDto;
import com.example.producr_service.application.dto.checkout.CheckoutItemSnapshotDto;
import com.example.producr_service.application.dto.checkout.CheckoutSaleDto;
import com.example.producr_service.application.dto.checkout.CheckoutVariantDto;
import com.example.producr_service.application.dto.saga.CheckoutInventoryRequestedPayload;
import com.example.producr_service.application.dto.saga.CheckoutInventorySucceededPayload;
import com.example.producr_service.application.dto.saga.CheckoutInventoryFailedPayload;
import com.example.producr_service.application.dto.saga.CheckoutInventoryRollbackRequestedPayload;
import com.example.producr_service.application.dto.saga.CheckoutInventoryRolledBackPayload;
import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.dto.outbox.ProductUpdatedOutboxPayload;
import com.example.producr_service.application.dto.saga.CheckoutSucceededPayload;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import com.example.producr_service.application.port.out.repo.FlashSaleCampaignRepositoryPort;
import com.example.producr_service.application.port.out.repo.FlashSaleItemRepositoryPort;
import com.example.producr_service.application.port.out.repo.InboxEventPort;
import com.example.producr_service.application.port.out.repo.ProductBehaviorRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import com.example.producr_service.domain.error.DomainProductError;
import com.example.producr_service.domain.model.BehaviorType;
import com.example.producr_service.domain.model.FlashSaleCampaignStatus;
import com.example.producr_service.domain.model.FlashSaleItemStatus;
import com.example.producr_service.domain.model.InboxEvent;
import com.example.producr_service.domain.model.ProductStatus;
import com.example.producr_service.domain.model.UserProductBehavior;
import com.example.producr_service.domain.service.FlashSaleRules;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class CheckoutInventoryService {
    private final InboxEventPort inbox;
    private final ProductRepositoryPort productRepositoryPort;
    private final ProductVariantRepoPort productVariantRepoPort;
    private final FlashSaleCampaignRepositoryPort flashSaleCampaignRepositoryPort;
    private final FlashSaleItemRepositoryPort flashSaleItemRepositoryPort;
    private final OutboxPort outbox;
    private final MapJsonToObjPort json;
    private final Clock clock;
    private final ShopAccessService shopAccessService;
    private final ProductBehaviorRepositoryPort productBehaviorRepositoryPort;

    // Đơn đã tạo thành công nên lỗi tại đây chỉ rollback cập nhật lượt mua và truyền về Kafka để xử lý DLT.
    @Transactional
    public void increaseCheckoutTotalSold(CheckoutSucceededPayload event) {
        validateMetadata(event.eventId(), event.checkoutId(), event.occurredAt(),
                CheckoutSucceededPayload.EVENT_TYPE.equals(event.eventType())
                        && event.inventorySucceededEventId() != null);
        if (!recordInboxEvent(event.eventId(), event.eventType(), event)) {
            return;
        }
        Map<Long, Long> quantities = findPurchasedQuantitiesByProduct(event);
        LocalDateTime now = LocalDateTime.now(clock);
        for (Map.Entry<Long, Long> entry : quantities.entrySet()) {
            if (productRepositoryPort.increaseTotalSold(entry.getKey(), entry.getValue()) != 1) {
                throw new IllegalStateException("Không tìm thấy sản phẩm để tăng lượt mua: " + entry.getKey());
            }
            outbox.save(new OutboxEventDto(UUID.randomUUID(), Constant.PRODUCT,
                    entry.getKey().toString(), ProductUpdatedOutboxPayload.EVENT_TYPE,
                    new ProductUpdatedOutboxPayload(entry.getKey()), now));
        }
        log.info("Đã tăng lượt mua: event_id={}, event_type={}, checkout_id={}, product_count={}",
                event.eventId(), event.eventType(), event.checkoutId(), quantities.size());
    }

    // Xử lý event checkout thành công: tăng lượt mua rồi ghi hành vi mua, cùng một transaction.
    @Transactional
    public void processCheckoutSucceeded(CheckoutSucceededPayload event) {
        increaseCheckoutTotalSold(event);
        recordPurchaseBehaviors(event);
    }

    // Lấy số lượng mua theo sản phẩm từ kết quả trừ tồn đã lưu tại Product, không tin dữ liệu bên gửi.
    private Map<Long, Long> findPurchasedQuantitiesByProduct(CheckoutSucceededPayload event) {
        OutboxEventDto saved = outbox.findByEventId(event.inventorySucceededEventId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy kết quả trừ tồn gốc"));
        CheckoutInventorySucceededPayload succeeded = json.readValue(saved.payload(), CheckoutInventorySucceededPayload.class);
        if (!CheckoutInventorySucceededPayload.EVENT_TYPE.equals(saved.eventType())
                || !event.inventorySucceededEventId().equals(succeeded.eventId())
                || !event.checkoutId().equals(succeeded.checkoutId())
                || succeeded.items() == null || succeeded.items().isEmpty()) {
            throw new IllegalArgumentException("Event tạo đơn không khớp thao tác trừ tồn");
        }
        Map<Long, Long> quantities = new TreeMap<>();
        for (CheckoutItemSnapshotDto item : succeeded.items()) {
            if (item.productId() == null || item.productId() <= 0 || item.quantity() <= 0) {
                throw new IllegalArgumentException("Số lượng mua trong kết quả trừ tồn không hợp lệ");
            }
            quantities.merge(item.productId(), (long) item.quantity(), Math::addExact);
        }
        return quantities;
    }

    // Ghi hành vi mua vào bảng hành vi người dùng; bản ghi trùng event_id bị bỏ qua nên retry an toàn.
    private void recordPurchaseBehaviors(CheckoutSucceededPayload event) {
        Instant createdAt = event.occurredAt().toInstant();
        if (event.userId() == null || event.userId() <= 0
                || !createdAt.isAfter(clock.instant().minus(UserProductBehavior.RETENTION))) {
            log.warn("Bỏ qua ghi hành vi mua: event_id={}, checkout_id={}", event.eventId(), event.checkoutId());
            return;
        }
        for (Long productId : findPurchasedQuantitiesByProduct(event).keySet()) {
            // Mỗi sản phẩm cần event_id riêng; sinh cố định theo event gốc để retry không tạo bản ghi trùng.
            String behaviorEventId = UUID.nameUUIDFromBytes(
                    (event.eventId() + ":" + productId).getBytes(StandardCharsets.UTF_8)).toString();
            productBehaviorRepositoryPort.saveIfAbsent(new UserProductBehavior(
                    behaviorEventId, event.userId(), productId, BehaviorType.PURCHASE, createdAt));
        }
    }

    // kiểm tra hàng giá tồn kho sản phẩm , sale v.v để tiến hành trừ tồn kho kho có event tử order
    @Transactional
    public void validateAndDeductCheckoutStock(CheckoutInventoryRequestedPayload event) {
        validateMetadata(event.eventId(), event.checkoutId(), event.occurredAt(),
                CheckoutInventoryRequestedPayload.EVENT_TYPE.equals(event.eventType()));

        if (!recordInboxEvent(event.eventId(), event.eventType(), event)) {
            return;
        }

        List<CheckoutItemSnapshotDto> items;
        Map<Long, CheckoutVariantDto> variants;
        Map<Long, Long> quantities;
        List<CheckoutSaleDto> sales;
        // lấy dữ liệu, khóa các bản ghi liên quan rồi kiểm tra giá/tồn kho
        try {
            quantities = validateAndSumQuantitiesByVariant(event.items());
            List<Long> saleVariants = event.items().stream().filter(CheckoutItemDto::sale)
                    .map(CheckoutItemDto::variantId).toList();
            List<Long> saleIds = flashSaleItemRepositoryPort.findCheckoutCandidates(saleVariants, LocalDateTime.now(clock));

            List<Long> campaignIds = flashSaleItemRepositoryPort.findCampaignIdsByItemIds(saleIds);

            // khóa chương trình sale for share
            flashSaleCampaignRepositoryPort.lockCampaignsForShare(campaignIds);
            List<Long> variantIds = new ArrayList<>(quantities.keySet());
            List<Long> productIds = productVariantRepoPort.findProductIdsByVariantIds(variantIds);
            // khóa các sản phẩm for share
            productRepositoryPort.lockProductsForShare(productIds);
            // khóa update các variant mà sẽ mua
            variants = productVariantRepoPort.lockVariantsForUpdate(variantIds).stream()
                    .collect(Collectors.toMap((CheckoutVariantDto value) -> value.variant().getId(), Function.identity()));
            // khóa các sản phầm đang sale for update
            sales = flashSaleItemRepositoryPort.lockItemsForUpdate(saleIds);
            items = validateAndBuildCheckoutItemSnapshots(event, quantities, variants, sales, LocalDateTime.now(clock));
        } catch (BusinessException exception) {
            if (exception.getErrorCode() == ProductError.SHOP_INFO_UNAVAILABLE) {
                throw exception;
            }
            publishFailed(event.checkoutId(), event.eventId(),
                    exception.getErrorCode().getCode(), exception.getMessage());
            return;
        }
        // ghi thay đổi tồn kho và số lượng sale
        for (Map.Entry<Long, Long> entry : quantities.entrySet()) {
            CheckoutVariantDto variant = variants.get(entry.getKey());
            variant.variant().decreaseStock(Math.toIntExact(entry.getValue()));
            productVariantRepoPort.updateStock(entry.getKey(), variant.variant().getStockQuantity());
        }
        Map<Long, CheckoutSaleDto> salesById = sales.stream()
                .collect(Collectors.toMap(CheckoutSaleDto::id, Function.identity()));
        LocalDateTime now = LocalDateTime.now(clock);
        for (CheckoutItemSnapshotDto item : items) {
            if (item.sale()) {
                CheckoutSaleDto sale = salesById.get(item.saleItemId());
                flashSaleItemRepositoryPort.updateSoldQuantity(sale.id(), Math.addExact(sale.soldQuantity(), item.quantity()), now);
            }
        }
        publishSucceeded(event, items);
        log.info("Đã trừ tồn checkout: event_id={}, checkout_id={}, item_count={}",
                event.eventId(), event.checkoutId(), items.size());
    }

    // Mua sale và mua thường dùng chung tồn kho nên cần gộp số lượng theo biến thể.
    private Map<Long, Long> validateAndSumQuantitiesByVariant(List<CheckoutItemDto> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException(ProductError.CHECKOUT_INVALID_ITEMS);
        }
        Set<Long> saleIds = new HashSet<>();
        Set<Long> normalIds = new HashSet<>();
        Map<Long, Long> quantities = new TreeMap<>();
        for (CheckoutItemDto item : items) {
            if (item == null || item.variantId() == null || item.variantId() <= 0
                    || item.quantity() == null || item.quantity() <= 0 || item.sale() == null
                    || !(item.sale() ? saleIds : normalIds).add(item.variantId())) {
                throw new BusinessException(ProductError.CHECKOUT_INVALID_ITEMS);
            }
            quantities.merge(item.variantId(), item.quantity().longValue(), Long::sum);
        }
        return quantities;
    }

    // Kiểm tra tồn kho, giá và sale rồi lưu thông tin từng dòng hàng để gửi sang Order tạo đơn.
    private List<CheckoutItemSnapshotDto> validateAndBuildCheckoutItemSnapshots(CheckoutInventoryRequestedPayload event,
            Map<Long, Long> quantities, Map<Long, CheckoutVariantDto> variants,
            List<CheckoutSaleDto> sales, LocalDateTime now) {
        shopAccessService.requireAvailableShops(variants.values().stream().map(CheckoutVariantDto::shopId).toList());
        // Kiểm tra tồn gộp để hai dòng sale và thường không cùng mua vượt kho của một biến thể.
        for (Map.Entry<Long, Long> entry : quantities.entrySet()) {
            CheckoutVariantDto variant = variants.get(entry.getKey());
            if (variant == null) {
                throw new BusinessException(ProductError.PRODUCT_VARIANT_NOT_FOUND);
            }
            if (variant.productStatus() != ProductStatus.ACTIVE) {
                throw new BusinessException(ProductError.PRODUCT_INACTIVE);
            }
            if (variant.variant().getStockQuantity() < entry.getValue()) {
                throw new BusinessException(DomainProductError.STOCK_INSUFFICIENT);
            }
        }
        List<CheckoutItemSnapshotDto> result = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CheckoutItemDto item : event.items()) {
            CheckoutVariantDto variant = variants.get(item.variantId());
            BigDecimal original = variant.variant().getPrice().getAmount();
            BigDecimal price = original;
            Long saleId = null;
            if (item.sale()) {
                List<CheckoutSaleDto> offers = sales.stream()
                        .filter((CheckoutSaleDto sale) -> item.variantId().equals(sale.variantId())
                                && sale.status() == FlashSaleItemStatus.REGISTERED
                                && sale.campaignStatus() == FlashSaleCampaignStatus.PUBLISHED
                                && !now.isBefore(sale.startsAt()) && now.isBefore(sale.endsAt())).toList();
                if (offers.size() != 1) {
                    throw new BusinessException(ProductError.CHECKOUT_SALE_UNAVAILABLE);
                }
                CheckoutSaleDto offer = offers.getFirst();
                if (offer.registeredQuantity() - offer.soldQuantity() < item.quantity()) {
                    throw new BusinessException(ProductError.CHECKOUT_SALE_UNAVAILABLE);
                }
                price = FlashSaleRules.calculateSalePrice(original, offer.discountPercent());
                saleId = offer.id();
            }
            result.add(new CheckoutItemSnapshotDto(variant.productId(), item.variantId(), variant.shopId(),
                    variant.productName(), variant.variantAttributes(), variant.imageUrl(), item.quantity(),
                    original, price, item.sale(), saleId));
            total = total.add(price.multiply(BigDecimal.valueOf(item.quantity())));
        }
        // Giá và ưu đãi có thể đã đổi nên tổng tính lại phải khớp số tiền khách xác nhận.
        if (event.totalAmount() == null || total.signum() <= 0 || total.compareTo(event.totalAmount()) != 0) {
            throw new BusinessException(ProductError.CHECKOUT_TOTAL_MISMATCH);
        }
        return List.copyOf(result);
    }

    // rollback lại tồn kho và sale nếu có khi order gửi lỗi sau khi prodcut đã trừ tồn kh
    @Transactional
    public void rollbackCheckoutStock(CheckoutInventoryRollbackRequestedPayload event) {
        validateMetadata(event.eventId(), event.checkoutId(), event.occurredAt(),
                CheckoutInventoryRollbackRequestedPayload.EVENT_TYPE.equals(event.eventType())
                        && event.inventorySucceededEventId() != null);
        if (!recordInboxEvent(event.eventId(), event.eventType(), event)) {
            return;
        }
        OutboxEventDto saved = outbox.findByEventId(event.inventorySucceededEventId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy kết quả trừ tồn gốc"));
        CheckoutInventorySucceededPayload succeeded = json.readValue(saved.payload(), CheckoutInventorySucceededPayload.class);
        if (!CheckoutInventorySucceededPayload.EVENT_TYPE.equals(saved.eventType())
                || !event.checkoutId().equals(succeeded.checkoutId())
                || succeeded.items() == null || succeeded.items().isEmpty()) {
            throw new IllegalArgumentException("Event rollback không khớp thao tác trừ tồn");
        }
        // Chỉ hoàn số lượng từ kết quả đã commit tại Product, không tin dữ liệu bên gửi.
        List<CheckoutItemSnapshotDto> items = succeeded.items();
        List<Long> saleIds = items.stream().filter(CheckoutItemSnapshotDto::sale)
                .map(CheckoutItemSnapshotDto::saleItemId).distinct().toList();
        List<Long> campaignIds = flashSaleItemRepositoryPort.findCampaignIdsByItemIds(saleIds);
        flashSaleCampaignRepositoryPort.lockCampaignsForShare(campaignIds);
        Map<Long, Long> quantities = new TreeMap<>();
        for (CheckoutItemSnapshotDto item : items) {
            quantities.merge(item.variantId(), (long) item.quantity(), Long::sum);
        }
        List<Long> variantIds = new ArrayList<>(quantities.keySet());
        List<Long> productIds = productVariantRepoPort.findProductIdsByVariantIds(variantIds);
        productRepositoryPort.lockProductsForShare(productIds);
        Map<Long, CheckoutVariantDto> variants = productVariantRepoPort.lockVariantsForUpdate(variantIds).stream()
                .collect(Collectors.toMap((CheckoutVariantDto value) -> value.variant().getId(), Function.identity()));
        Map<Long, CheckoutSaleDto> sales = flashSaleItemRepositoryPort.lockItemsForUpdate(saleIds).stream()
                .collect(Collectors.toMap(CheckoutSaleDto::id, Function.identity()));
        for (Map.Entry<Long, Long> entry : quantities.entrySet()) {
            CheckoutVariantDto variant = variants.get(entry.getKey());
            if (variant == null) {
                log.info("Bỏ qua hoàn tồn phân loại đã xóa: event_id={}, checkout_id={}, variant_id={}",
                        event.eventId(), event.checkoutId(), entry.getKey());
                continue;
            }
            if ((long) variant.variant().getStockQuantity() + entry.getValue() > Integer.MAX_VALUE) {
                throw new IllegalStateException("Không thể hoàn tồn phân loại: " + entry.getKey());
            }
            variant.variant().increaseStock(Math.toIntExact(entry.getValue()));
            productVariantRepoPort.updateStock(entry.getKey(), variant.variant().getStockQuantity());
        }
        for (CheckoutItemSnapshotDto item : items) {
            if (item.sale()) {
                CheckoutSaleDto sale = sales.get(item.saleItemId());
                // FK sale có thể null sau khi xóa variant, nhưng quota vẫn phải được hoàn.
                if (sale == null || (sale.variantId() != null && !item.variantId().equals(sale.variantId()))
                        || sale.soldQuantity() < item.quantity()) {
                    throw new IllegalStateException("Không thể hoàn quota sale: " + item.saleItemId());
                }
                flashSaleItemRepositoryPort.updateSoldQuantity(sale.id(), sale.soldQuantity() - item.quantity(), LocalDateTime.now(clock));
            }
        }
        decreaseTotalSoldIfCounted(event, items);
        LocalDateTime now = LocalDateTime.now(clock);
        for (Long productId : productIds.stream().distinct().toList()) {
            outbox.save(new OutboxEventDto(UUID.randomUUID(), Constant.PRODUCT, productId.toString(),
                    ProductUpdatedOutboxPayload.EVENT_TYPE, new ProductUpdatedOutboxPayload(productId), now));
        }
        publishRolledBack(event);
        log.info("Đã rollback tồn kho checkout: event_id={}, event_type={}, checkout_id={}, item_count={}",
                event.eventId(), event.eventType(), event.checkoutId(), items.size());
    }

    // CHECKOUT_SUCCEEDED tới muộn hơn rollback sẽ thấy inbox đã có và bỏ qua, nên không tăng lượt bán sai.
    private void decreaseTotalSoldIfCounted(CheckoutInventoryRollbackRequestedPayload event,
                                            List<CheckoutItemSnapshotDto> items) {
        if (event.checkoutSucceededEventId() == null) {
            return;
        }
        InboxEvent marker = InboxEvent.create(event.checkoutSucceededEventId(), CheckoutSucceededPayload.EVENT_TYPE,
                json.writeValue(event), LocalDateTime.now(clock));
        if (inbox.tryInsert(marker)) {
            log.info("CHECKOUT_SUCCEEDED chưa được xử lý nên không cần trừ lượt bán: event_id={}, checkout_id={}",
                    event.eventId(), event.checkoutId());
            return;
        }
        Map<Long, Long> quantities = new TreeMap<>();
        for (CheckoutItemSnapshotDto item : items) {
            quantities.merge(item.productId(), (long) item.quantity(), Math::addExact);
        }
        for (Map.Entry<Long, Long> entry : quantities.entrySet()) {
            if (productRepositoryPort.decreaseTotalSold(entry.getKey(), entry.getValue()) != 1) {
                log.warn("Bỏ qua trừ lượt bán: event_id={}, checkout_id={}, product_id={}",
                        event.eventId(), event.checkoutId(), entry.getKey());
            }
        }
    }

    // Ghi Inbox để tránh xử lý lặp; trả về false nếu eventId đã tồn tại.
    private boolean recordInboxEvent(UUID eventId, String eventType, Object payload) {
        InboxEvent inboxEvent = InboxEvent.create(eventId, eventType,
                json.writeValue(payload), LocalDateTime.now(clock));
        if (!inbox.tryInsert(inboxEvent)) {
            log.info("Bỏ qua event đã xử lý: event_id={}, event_type={}", eventId, eventType);
            return false;
        }
        return true;
    }

    private void publishSucceeded(CheckoutInventoryRequestedPayload event, List<CheckoutItemSnapshotDto> items) {
        LocalDateTime now = LocalDateTime.now(clock);
        UUID eventId = UUID.randomUUID();
        CheckoutInventorySucceededPayload result = new CheckoutInventorySucceededPayload(eventId,
                CheckoutInventorySucceededPayload.EVENT_TYPE, event.checkoutId(), event.eventId(),
                items, event.totalAmount(), now.atZone(clock.getZone()).toOffsetDateTime());
        outbox.save(new OutboxEventDto(eventId, Constant.CHECKOUT, event.checkoutId().toString(),
                result.eventType(), result, now));
    }

    // gửi event lỗi khi có lỗi xay ra trong quá trình
    private void publishFailed(Long checkoutId, UUID requestEventId, String reasonCode, String reason) {
        LocalDateTime now = LocalDateTime.now(clock);
        UUID eventId = UUID.randomUUID();
        CheckoutInventoryFailedPayload result = new CheckoutInventoryFailedPayload(eventId,
                CheckoutInventoryFailedPayload.EVENT_TYPE, checkoutId, requestEventId, reasonCode, reason,
                now.atZone(clock.getZone()).toOffsetDateTime());
        outbox.save(new OutboxEventDto(eventId, Constant.CHECKOUT, checkoutId.toString(),
                result.eventType(), result, now));
        log.warn("Từ chối checkout: event_id={}, checkout_id={}, reason_code={}",
                requestEventId, checkoutId, result.reasonCode());
    }

    // gửi event rollback thành công về cho order service
    private void publishRolledBack(CheckoutInventoryRollbackRequestedPayload event) {
        LocalDateTime now = LocalDateTime.now(clock);
        UUID eventId = UUID.randomUUID();
        CheckoutInventoryRolledBackPayload result = new CheckoutInventoryRolledBackPayload(eventId,
                CheckoutInventoryRolledBackPayload.EVENT_TYPE, event.checkoutId(), event.eventId(),
                event.reasonCode(), event.reason(), now.atZone(clock.getZone()).toOffsetDateTime());
        outbox.save(new OutboxEventDto(eventId, Constant.CHECKOUT, event.checkoutId().toString(),
                result.eventType(), result, now));
    }

    private void validateMetadata(UUID eventId, Long checkoutId, OffsetDateTime occurredAt, boolean supported) {
        if (eventId == null || checkoutId == null || checkoutId <= 0 || occurredAt == null || !supported) {
            throw new IllegalArgumentException("Metadata event tồn kho checkout không hợp lệ");
        }
    }
}
