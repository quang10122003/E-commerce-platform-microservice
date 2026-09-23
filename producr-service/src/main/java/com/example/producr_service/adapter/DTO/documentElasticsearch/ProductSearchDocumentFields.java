package com.example.producr_service.adapter.DTO.documentElasticsearch;

// Tập trung tên field của product document để dùng thống nhất khi mapping và query ES.
public final class ProductSearchDocumentFields {

    public static final String PRODUCT_ID = "productId";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String CATEGORY_ID = "categoryId";
    public static final String CATEGORY_NAME = "categoryName";
    public static final String BRAND_ID = "brandId";
    public static final String BRAND_NAME = "brandName";
    public static final String STATUS = "status";
    public static final String IMAGE_URL = "imageUrl";
    public static final String TOTAL_SOLD = "totalSold";
    public static final String MIN_PRICE = "minPrice";
    public static final String MAX_PRICE = "maxPrice";
    public static final String CREATED_AT = "createdAt";
    public static final String ATTRIBUTES = "attributes";
    public static final String VARIANTS = "variants";
    public static final String LOCATION = "location";

    private ProductSearchDocumentFields() {
    }
}
