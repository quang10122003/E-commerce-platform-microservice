package com.example.producr_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.producr_service.application.dto.request.CreateProductRequest;
import com.example.producr_service.application.dto.response.ProductResponse;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.out.BrandRepositoryPort;
import com.example.producr_service.application.port.out.CategoryRepoPort;
import com.example.producr_service.application.port.out.ProductRepositoryPort;
import com.example.producr_service.domain.model.*;
import com.example.producr_service.domain.service.SkuGenerator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
// Chịu trách nhiệm tạo, kiểm tra và lưu aggregate Product.
public class ProductService {

    private static final int MAX_SKU_RETRY = 10;
    private static final int RANDOM_SUFFIX_LENGTH = 4;

    ProductRepositoryPort productRepositoryPort;
    CategoryRepoPort categoryRepositoryPort;
    BrandRepositoryPort brandRepositoryPort;
    @Transactional
    // Tạo và lưu aggregate Product từ request đã hoàn tất dữ liệu ảnh.
    public ProductResponse createProduct(CreateProductRequest request) {

        // check danh mục bạn brand tồn tại chưa
        ValitionCatgoryAndBrand_Notexist(request.getCategoryId(),request.getBrandId());


        Product product = new Product(null, request.getCategoryId(), request.getBrandId(),
                request.getName(), request.getDescription(), request.getImageUrl());

        //list  TẤT CẢ các valuesOfThisAttribute lại, theo đúng thứ tự attribute
        List<List<AttributeValue>> valuesByIndex = new ArrayList<>();

        for (CreateProductRequest.AttributeRequest attrReq : request.getAttributes()) {
            ProductAttribute attribute = new ProductAttribute(null, attrReq.getName());

            // list lưu AttributeValue của 1 ProductAttribute cụ thể
            List<AttributeValue> valuesOfThisAttribute = new ArrayList<>();

            for (String attributevalue : attrReq.getValues()) {
                AttributeValue value = new AttributeValue(null, attributevalue);
                attribute.addValue(value);
                valuesOfThisAttribute.add(value);
            }
            product.addAttribute(attribute);

            valuesByIndex.add(valuesOfThisAttribute);
        }

        Set<String> skusUsedInThisRequest = new HashSet<>();

        for (CreateProductRequest.VariantRequest vReq : request.getVariants()) {

            List<AttributeValue> selectedValues = vReq.getAttributeSelections().stream()
                    .map(sel -> resolveAttributeValue(valuesByIndex, sel))
                    .collect(Collectors.toList());

            String sku = generateUniqueSku(request.getName(), selectedValues, skusUsedInThisRequest);
            skusUsedInThisRequest.add(sku);

            ProductVariant variant = new ProductVariant(
                    null, sku, Money.of(vReq.getPrice()), vReq.getStockQuantity());

            for (AttributeValue value : selectedValues) {
                variant.linkAttributeValue(value);
            }

            for (CreateProductRequest.VariantImageRequest imgReq : vReq.getImages()) {
                variant.addImage(new VariantImage(null, imgReq.getImageUrl(), imgReq.isPrimary()));
            }

            product.addVariant(variant);
        }

        // Buoc 5 - luu toan bo aggregate trong 1 transaction
        Product saved = productRepositoryPort.save(product);

        // Buoc 6 - map Domain -> DTO NGAY TAI DAY, vi ProductResponse gio
        // thuoc application layer (cung tang voi UseCase), khong con
        // thuoc adapter nua - tra thang DTO, Controller khong can map lai
        return ProductResponse.from(saved);
    }

    private String generateUniqueSku(String productName, List<AttributeValue> selectedValues,
                                     Set<String> skusUsedInThisRequest) {
        List<String> valueTexts = selectedValues.stream()
                .map(AttributeValue::getValue)
                .collect(Collectors.toList());

        String baseSku = SkuGenerator.generateBase(productName, valueTexts);
        String candidate = baseSku;

        int attempt = 0;
        while (skusUsedInThisRequest.contains(candidate) || productRepositoryPort.existsBySku(candidate)) {
            attempt++;
            if (attempt > MAX_SKU_RETRY) {
                throw new IllegalStateException(
                        "Khong the sinh SKU duy nhat cho '" + baseSku + "' sau " + MAX_SKU_RETRY + " lan thu");
            }
            candidate = baseSku + "-" + SkuGenerator.randomSuffix(RANDOM_SUFFIX_LENGTH);
        }
        return candidate;
    }

    // check danh mục và brad tồn tại hay chưa
    private void ValitionCatgoryAndBrand_Notexist(Long categoryId, Long brandId){
        if (!categoryRepositoryPort.existsById(categoryId)) {
            throw new BusinessException(ProductError.CATEGORY_NOT_FOUND);
        }
        if (brandId != null && !brandRepositoryPort.existsById(brandId)) {
            throw new BusinessException(ProductError.BRAND_NOT_FOUND);
        }
    }
    // đổi (attributeIndex, valueIndex) client gui thanh dung AttributeValue tuong ung, dong thoi validate index hop le
    private AttributeValue resolveAttributeValue(List<List<AttributeValue>> valuesByIndex,
                                                 CreateProductRequest.AttributeSelection sel) {
        int attributeIndex = sel.getAttributeIndex();
        int valueIndex = sel.getValueIndex();

        // Kiểm tra chỉ số thuộc tính trước khi truy cập danh sách
        if (attributeIndex < 0 || attributeIndex >= valuesByIndex.size()) {
            throw invalidAttributeSelection(attributeIndex, valueIndex);
        }
        List<AttributeValue> values = valuesByIndex.get(attributeIndex);

        // Kiểm tra chỉ số giá trị trước khi lấy phần tử trong thuộc tính đã chọn.
        if (valueIndex < 0 || valueIndex >= values.size()) {
            throw invalidAttributeSelection(attributeIndex, valueIndex);
        }
        return values.get(valueIndex);
    }

    // Tạo BusinessException kèm attributeIndex và valueIndex không hợp lệ để phản hồi chỉ rõ lỗi trong request.
    private BusinessException invalidAttributeSelection(int attributeIndex, int valueIndex) {
        return new BusinessException(
                ProductError.INVALID_ATTRIBUTE_SELECTION,
                "attributeIndex=" + attributeIndex + ", valueIndex=" + valueIndex);
    }
}
