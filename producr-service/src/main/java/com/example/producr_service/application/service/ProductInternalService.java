package com.example.producr_service.application.service;

import com.example.producr_service.application.dto.request.CartInfoRequest;
import com.example.producr_service.application.dto.response.CartInfoRepone;
import com.example.producr_service.application.dto.response.VariantCartView;
import com.example.producr_service.application.port.in.GetCartInfoUseCase;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ProductInternalService implements GetCartInfoUseCase {
    ProductVariantRepoPort productVariantRepoPort;



    // Lấy thông tin sản phẩm theo danh sách variant để phục vụ hiển thị giỏ hàng.
    @Override
    @Transactional(readOnly = true)
    public List<CartInfoRepone> getCartInfo(CartInfoRequest request) {
        return productVariantRepoPort.findCartViewsByVariantIds(request.variantIds()).stream()
                .map(this::toCartInfoRepone)
                .toList();
    }

    private CartInfoRepone toCartInfoRepone(VariantCartView view){
        List<CartInfoRepone.VariantOption> options  = view.options().stream().sorted(Comparator.comparing(VariantCartView.OptionView::attributeId).thenComparing(VariantCartView.OptionView::valueId)).map(optionView -> new CartInfoRepone.VariantOption(optionView.name(),optionView.value())).toList();

        boolean hasOption = !options.isEmpty();

        // sản phẩm có  phân loai thì uw tiên lấy ảnh phân lọai , ko thì lấy ảnh product
        String imgUrl = hasOption ? pickVariantImage(view.images()).orElse(view.productImageUrl()) : view.productImageUrl();
        return  new CartInfoRepone(
                view.variantId(), view.productId(), view.productName(),
                imgUrl, hasOption, options, view.price(), view.stockQuantity(),
                view.status());

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
