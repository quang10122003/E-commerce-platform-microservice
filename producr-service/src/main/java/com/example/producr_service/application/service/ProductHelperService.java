package com.example.producr_service.application.service;

import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.response.SellerProductItemResponse;
import com.example.producr_service.domain.model.AttributeValue;
import com.example.producr_service.domain.model.Product;
import com.example.producr_service.domain.model.ProductAttribute;
import com.example.producr_service.domain.model.ProductStatus;
import com.example.producr_service.domain.model.ProductVariant;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Dựng dữ liệu trang quản lý sản phẩm từ các model đã được repository trả về.
public class ProductHelperService {

    // Lấy ID của các sản phẩm trong trang để truy vấn phân loại theo lô.
    public List<Long> getListIdProduct(Collection<Product> products) {
        return products.stream().map(Product::getId).toList();
    }

    // Dựng trang response và giữ nguyên thông tin phân trang từ trang sản phẩm.
    public PageResponse<SellerProductItemResponse> buildSellerProductPage(
            PageResponse<Product> productPage,
            Map<Long, List<ProductVariant>> variantsByProductId
    ) {
        List<SellerProductItemResponse> items = buildListSellerProductItemResponse(
                productPage.items(), variantsByProductId);
        return new PageResponse<>(items, productPage.page(), productPage.pageSize(),
                productPage.totalItems(), productPage.totalPages());
    }

    // Ghép từng sản phẩm với các phân loại đã tải bằng ID sản phẩm.
    public List<SellerProductItemResponse> buildListSellerProductItemResponse(
            List<Product> products,
            Map<Long, List<ProductVariant>> variantsByProductId
    ) {
        return products.stream()
                .map(product -> toSellerItem(product,
                        variantsByProductId.getOrDefault(product.getId(), List.of())))
                .toList();
    }

    // Chuyển sản phẩm và phân loại sang một dòng quản lý của người bán.
    private SellerProductItemResponse toSellerItem(Product product, List<ProductVariant> variants) {
        Map<Long, ProductAttribute> attributeByValueId = product.getAttributes().stream()
                .flatMap(attribute -> attribute.getValues().stream()
                        .map(value -> Map.entry(value.getId(), attribute)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<SellerProductItemResponse.Variant> variantResponses = variants.stream()
                .sorted(Comparator.comparing(ProductVariant::getId))
                .map(variant -> toSellerVariant(variant, attributeByValueId))
                .toList();

        // Hết hàng được ưu tiên hiển thị cho cả sản phẩm đang bán và đang ẩn.
        boolean inStock = variants.stream().anyMatch(ProductVariant::isInStock);
        SellerProductItemResponse.DisplayStatus displayStatus = !inStock
                ? SellerProductItemResponse.DisplayStatus.OUT_OF_STOCK
                : product.getStatus() == ProductStatus.INACTIVE
                        ? SellerProductItemResponse.DisplayStatus.INACTIVE
                        : SellerProductItemResponse.DisplayStatus.ACTIVE;

        return new SellerProductItemResponse(
                product.getId(), product.getName(), product.getImageUrl(), product.getCreatedAt(),
                product.getCategoryId(), product.getCategoryName(), displayStatus, variantResponses);
    }

    // Tra tên thuộc tính của từng giá trị đã chọn trong phân loại.
    private SellerProductItemResponse.Variant toSellerVariant(
            ProductVariant variant,
            Map<Long, ProductAttribute> attributeByValueId
    ) {
        List<SellerProductItemResponse.VariantAttribute> attributes = variant.getAttributeValues().stream()
                .map(value -> toSellerVariantAttribute(value, attributeByValueId))
                .sorted(Comparator.comparing(SellerProductItemResponse.VariantAttribute::attributeId))
                .toList();

        return new SellerProductItemResponse.Variant(
                variant.getId(), variant.getSku(), variant.getPrice().getAmount(),
                variant.getStockQuantity(), attributes);
    }

    // Tìm thuộc tính cha theo ID giá trị để trả đúng tên và ID thuộc tính.
    private SellerProductItemResponse.VariantAttribute toSellerVariantAttribute(
            AttributeValue value,
            Map<Long, ProductAttribute> attributeByValueId
    ) {
        ProductAttribute attribute = attributeByValueId.get(value.getId());
        if (attribute == null) {
            throw new IllegalStateException("Không tìm thấy thuộc tính của giá trị id=" + value.getId());
        }
        return new SellerProductItemResponse.VariantAttribute(
                attribute.getId(), attribute.getName(), value.getValue());
    }
}
