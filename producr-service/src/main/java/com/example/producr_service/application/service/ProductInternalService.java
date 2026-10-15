package com.example.producr_service.application.service;

import com.example.producr_service.application.dto.response.CartInfoRepone;
import com.example.producr_service.application.dto.response.ShopInfoResponse;
import com.example.producr_service.application.dto.response.VariantCartView;
import com.example.producr_service.application.dto.response.FlashSaleOfferView;
import com.example.producr_service.application.dto.response.VariantSaleResponse;
import com.example.producr_service.application.port.out.repo.FlashSaleItemRepositoryPort;
import com.example.producr_service.domain.model.ProductStatus;
import com.example.producr_service.application.port.in.GetCartInfoUseCase;
import com.example.producr_service.application.port.out.client.ShopInfoClientPort;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ProductInternalService implements GetCartInfoUseCase {
    ProductVariantRepoPort productVariantRepoPort;
    ShopInfoClientPort shopInfoClientPort;
    FlashSaleItemRepositoryPort flashSaleItemRepositoryPort;
    VariantSaleResolver variantSaleResolver;
    Clock clock;



    // Lấy thông tin sản phẩm theo danh sách variant để phục vụ hiển thị giỏ hàng.
    @Override
    @Transactional(readOnly = true)
    public List<CartInfoRepone> getCartInfo(List<Long> variantIds) {
        List<VariantCartView> views = productVariantRepoPort.findCartViewsByVariantIds(variantIds);
        if (views.isEmpty()) {
            return List.of();
        }

        List<Long> shopIds = views.stream().map(VariantCartView::shopId).distinct().toList();
        Map<Long, ShopInfoResponse> shopInfos = shopInfoClientPort.getShopInfos(shopIds).stream()
                .collect(Collectors.toMap(ShopInfoResponse::shopId, Function.identity()));

        // Lấy sale một lần cho các variant công khai và dùng chung mốc thời gian.

        List<Long> activeVariantIds = views.stream()
                .filter(view -> view.status() == ProductStatus.ACTIVE)
                .map(VariantCartView::variantId).distinct().toList();
        Map<Long, List<FlashSaleOfferView>> offersByVariantId = flashSaleItemRepositoryPort
                .findOngoingOffersByVariantIds(activeVariantIds, LocalDateTime.now(clock)).stream()
                .collect(Collectors.groupingBy(FlashSaleOfferView::variantId));
        return views.stream()
                .map(view -> toCartInfoRepone(view, shopInfos.get(view.shopId()),
                        offersByVariantId.getOrDefault(view.variantId(), List.of())))
                .toList();
    }

    // Ghép tên shop và sale vào thông tin variant trước khi trả cho Order service.
    private CartInfoRepone toCartInfoRepone(VariantCartView view, ShopInfoResponse shopInfo,
                                          List<FlashSaleOfferView> offers){
        List<CartInfoRepone.VariantOption> options  = view.options().stream().sorted(Comparator.comparing(VariantCartView.OptionView::attributeId).thenComparing(VariantCartView.OptionView::valueId)).map(optionView -> new CartInfoRepone.VariantOption(optionView.name(),optionView.value())).toList();

        boolean hasOption = !options.isEmpty();

        // sản phẩm có  phân loai thì uw tiên lấy ảnh phân lọai , ko thì lấy ảnh product
        String imgUrl = hasOption ? pickVariantImage(view.images()).orElse(view.productImageUrl()) : view.productImageUrl();
        // Giữ sản phẩm ngừng hoạt động trong giỏ nhưng không hiển thị sale.
        VariantSaleResponse.Sale applicableSale = view.status() == ProductStatus.ACTIVE
                ? variantSaleResolver.resolve(view.price(), view.stockQuantity(), offers) : null;
        CartInfoRepone.Sale sale = applicableSale == null ? null
                : new CartInfoRepone.Sale(applicableSale.salePrice(), applicableSale.endsAt());
        return  new CartInfoRepone(
                view.variantId(), view.productId(), view.shopId(),
                shopInfo == null ? null : shopInfo.shopName(), view.productName(),
                imgUrl, hasOption, options, view.price(), view.stockQuantity(),
                view.status(), sale);

    }

    // lấy ảnh chỉnh cửa 1 Variant nếu k có thì lấy ảnh đầu
    private Optional<String> pickVariantImage(List<VariantCartView.ImageView> images) {
        return images.stream()
                .filter(VariantCartView.ImageView::primary)
                        .findFirst()
                .or(() -> images.stream().findFirst())
                .map(VariantCartView.ImageView::url);
    }
}
