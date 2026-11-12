package com.example.order_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.order_service.application.dto.analytics.DailyTotals;
import com.example.order_service.application.dto.analytics.DurationAverage;
import com.example.order_service.application.dto.analytics.MoneyTally;
import com.example.order_service.application.dto.order.ShopAnalyticsOrderView;
import com.example.order_service.application.dto.response.AdminAnalyticsResponse;
import com.example.order_service.application.dto.response.ShopAnalyticsResponse;
import com.example.order_service.application.dto.response.ShopAnalyticsResponse.CancelSlice;
import com.example.order_service.application.dto.response.ShopAnalyticsResponse.DailyPoint;
import com.example.order_service.application.dto.response.ShopAnalyticsResponse.Overview;
import com.example.order_service.application.dto.response.ShopAnalyticsResponse.ProcessingTime;
import com.example.order_service.application.dto.response.ShopAnalyticsResponse.Slice;
import com.example.order_service.application.error.OrderError;
import com.example.order_service.application.port.in.GetAdminAnalyticsUseCase;
import com.example.order_service.application.port.in.GetShopAnalyticsUseCase;
import com.example.order_service.application.port.out.client.CurrentUserPort;
import com.example.order_service.application.port.out.repo.ShopAnalyticsQueryPort;
import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.OrderCancelReason;
import com.example.order_service.domain.model.OrderCancelledBy;
import com.example.order_service.domain.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// shortcut: tải từng dòng đơn của kỳ rồi cộng trong Java, chuyển sang group by ở SQL khi một shop vượt khoảng 50.000 đơn mỗi kỳ.
@RequiredArgsConstructor
public class ShopAnalyticsService implements GetShopAnalyticsUseCase, GetAdminAnalyticsUseCase {
    private static final int MAX_RANGE_DAYS = 366;
    private static final int TOP_PRODUCT_LIMIT = 10;
    private static final int TOP_SHOP_LIMIT = 10;

    private final CurrentUserPort currentUserPort;
    private final ShopAnalyticsQueryPort queryPort;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public ShopAnalyticsResponse getShopAnalytics(OffsetDateTime from, OffsetDateTime to) {
        validateRange(from, to);
        return analyze(currentUserPort.getInfo().userId(), from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminAnalyticsResponse getAdminAnalytics(OffsetDateTime from, OffsetDateTime to) {
        validateRange(from, to);
        ShopAnalyticsResponse summary = analyze(null, from, to);
        LocalDateTime start = toStoredTime(from);
        LocalDateTime end = toStoredTime(to);
        return new AdminAnalyticsResponse(summary, queryPort.countActiveShops(start, end),
                queryPort.findTopShops(start, end, TOP_SHOP_LIMIT));
    }

    // shopId null nghĩa là gộp mọi shop trên sàn.
    private ShopAnalyticsResponse analyze(Long shopId, OffsetDateTime from, OffsetDateTime to) {
        LocalDateTime start = toStoredTime(from);
        LocalDateTime end = toStoredTime(to);
        // Kỳ trước dài bằng kỳ đang xem và kết thúc ngay trước nó; DB lưu thời gian tới micro giây.
        LocalDateTime previousEnd = start.minus(1, ChronoUnit.MICROS);
        LocalDateTime previousStart = previousEnd.minus(Duration.between(start, end));
        List<ShopAnalyticsOrderView> orders = queryPort.findOrdersTouching(shopId, previousStart, end);
        // Ngày và giờ gom theo offset của người xem để cột ngày khớp lịch trên trình duyệt.
        ZoneOffset viewerOffset = from.getOffset();

        Map<LocalDate, DailyTotals> days = new LinkedHashMap<>();
        LocalDate lastDay = to.withOffsetSameInstant(viewerOffset).toLocalDate();
        for (LocalDate day = from.toLocalDate(); !day.isAfter(lastDay); day = day.plusDays(1)) {
            days.put(day, new DailyTotals());
        }
        long[] ordersByHour = new long[24];
        Map<String, MoneyTally> byStatus = new LinkedHashMap<>();
        Map<String, MoneyTally> byPaymentMethod = new LinkedHashMap<>();
        Map<String, MoneyTally> byPriceType = new LinkedHashMap<>();
        Map<List<Enum<?>>, MoneyTally> cancellations = new LinkedHashMap<>();
        DurationAverage confirm = new DurationAverage();
        DurationAverage handover = new DurationAverage();
        DurationAverage delivery = new DurationAverage();

        for (ShopAnalyticsOrderView order : orders) {
            BigDecimal amount = order.totalAmount();
            if (within(order.createdAt(), start, end)) {
                OffsetDateTime createdAt = toViewerTime(order.createdAt(), viewerOffset);
                days.get(createdAt.toLocalDate()).addPlaced(amount);
                ordersByHour[createdAt.getHour()]++;
                tally(byStatus, order.status().name(), amount);
                if (order.status() != OrderStatus.CANCEL) {
                    tally(byPaymentMethod, order.paymentMethod().name(), amount);
                    tally(byPriceType, order.priceType().name(), amount);
                }
                confirm.add(order.createdAt(), order.confirmedAt());
                handover.add(order.confirmedAt(), order.shippedAt());
                delivery.add(order.shippedAt(), order.deliveredAt());
            }
            if (order.status() == OrderStatus.DONE && within(order.deliveredAt(), start, end)) {
                days.get(toViewerTime(order.deliveredAt(), viewerOffset).toLocalDate()).addReceived(amount);
            }
            if (within(order.paidAt(), start, end)) {
                days.get(toViewerTime(order.paidAt(), viewerOffset).toLocalDate()).addPaidOnline(amount);
            }
            if (order.status() == OrderStatus.CANCEL && within(order.cancelledAt(), start, end)) {
                days.get(toViewerTime(order.cancelledAt(), viewerOffset).toLocalDate()).addCancelled(amount);
                cancellations.computeIfAbsent(Arrays.asList(order.cancelledBy(), order.cancelReason()),
                        (List<Enum<?>> key) -> new MoneyTally()).add(amount);
            }
        }

        List<DailyPoint> timeline = new ArrayList<>();
        for (Map.Entry<LocalDate, DailyTotals> entry : days.entrySet()) {
            timeline.add(entry.getValue().toPoint(entry.getKey().atStartOfDay().atOffset(viewerOffset)));
        }
        List<CancelSlice> cancelSlices = new ArrayList<>();
        for (Map.Entry<List<Enum<?>>, MoneyTally> entry : cancellations.entrySet()) {
            cancelSlices.add(new CancelSlice((OrderCancelledBy) entry.getKey().get(0),
                    (OrderCancelReason) entry.getKey().get(1), entry.getValue().getCount(), entry.getValue().getAmount()));
        }
        return new ShopAnalyticsResponse(from, to, overview(orders, start, end),
                overview(orders, previousStart, previousEnd), queryPort.sumSaleDiscount(shopId, start, end), timeline,
                Arrays.stream(ordersByHour).boxed().toList(), toSlices(byStatus), toSlices(byPaymentMethod),
                toSlices(byPriceType), cancelSlices,
                new ProcessingTime(confirm.minutes(), handover.minutes(), delivery.minutes()),
                queryPort.findTopProducts(shopId, start, end, TOP_PRODUCT_LIMIT));
    }

    // Mỗi đơn chỉ thuộc một nhóm tiền theo trạng thái hiện tại, xét trên mốc thời gian của nhóm đó.
    private Overview overview(List<ShopAnalyticsOrderView> orders, LocalDateTime start, LocalDateTime end) {
        MoneyTally placed = new MoneyTally();
        MoneyTally received = new MoneyTally();
        MoneyTally held = new MoneyTally();
        MoneyTally expectedCod = new MoneyTally();
        MoneyTally awaitingPayment = new MoneyTally();
        MoneyTally lost = new MoneyTally();
        MoneyTally refundDue = new MoneyTally();
        Set<Long> buyers = new HashSet<>();
        for (ShopAnalyticsOrderView order : orders) {
            BigDecimal amount = order.totalAmount();
            boolean placedInRange = within(order.createdAt(), start, end);
            if (placedInRange) {
                placed.add(amount);
                if (order.buyerId() != null) {
                    buyers.add(order.buyerId());
                }
            }
            if (order.status() == OrderStatus.DONE) {
                if (within(order.deliveredAt(), start, end)) {
                    received.add(amount);
                }
            } else if (order.status() == OrderStatus.CANCEL) {
                if (within(order.cancelledAt(), start, end)) {
                    lost.add(amount);
                    if (order.paidAt() != null) {
                        refundDue.add(amount);
                    }
                }
            } else if (order.paidAt() != null) {
                if (within(order.paidAt(), start, end)) {
                    held.add(amount);
                }
            } else if (placedInRange) {
                (order.paymentMethod() == CheckoutPaymentMethod.COD ? expectedCod : awaitingPayment).add(amount);
            }
        }
        return new Overview(placed.toMoney(), received.toMoney(), held.toMoney(), expectedCod.toMoney(),
                awaitingPayment.toMoney(), lost.toMoney(), refundDue.toMoney(), buyers.size());
    }

    private static void validateRange(OffsetDateTime from, OffsetDateTime to) {
        if (from.isAfter(to)) {
            throw new BusinessException(OrderError.INVALID_DATE_RANGE);
        }
        if (Duration.between(from, to).toDays() >= MAX_RANGE_DAYS) {
            throw new BusinessException(OrderError.ANALYTICS_RANGE_TOO_LONG);
        }
    }

    // DB lưu LocalDateTime theo múi giờ của Clock nên đổi mốc có offset về cùng múi giờ trước khi so sánh.
    private LocalDateTime toStoredTime(OffsetDateTime value) {
        return value.atZoneSameInstant(clock.getZone()).toLocalDateTime();
    }

    private OffsetDateTime toViewerTime(LocalDateTime value, ZoneOffset viewerOffset) {
        return value.atZone(clock.getZone()).toOffsetDateTime().withOffsetSameInstant(viewerOffset);
    }

    private static boolean within(LocalDateTime value, LocalDateTime start, LocalDateTime end) {
        return value != null && !value.isBefore(start) && !value.isAfter(end);
    }

    private static void tally(Map<String, MoneyTally> totals, String key, BigDecimal amount) {
        totals.computeIfAbsent(key, (String ignored) -> new MoneyTally()).add(amount);
    }

    private static List<Slice> toSlices(Map<String, MoneyTally> totals) {
        return totals.entrySet().stream()
                .map((Map.Entry<String, MoneyTally> entry) -> new Slice(entry.getKey(), entry.getValue().getCount(),
                        entry.getValue().getAmount()))
                .toList();
    }
}
