package com.example.producr_service.application.constant;

public final class Constant {
    public static final String PRODUCT = "Product";
    public static final String VARIANT = "Variant";
    // Tên index tìm kiếm sản phẩm trên Elasticsearch.
    public static final String SEARCH_INDEX = "product_search";
    // Tên bucket lưu ảnh danh mục trên storage.
    public static final String CATEGORY_BUCKET = "category";
    // Tên bucket lưu ảnh bìa sản phẩm.
    public static final String PRODUCT_IMAGES_BUCKET = "product-images";
    // Tên bucket lưu ảnh của phân loại sản phẩm.
    public static final String PRODUCT_VARIANTS_BUCKET = "product-variants";
    // Đường dẫn API thao tác object trên Supabase Storage.
    public static final String STORAGE_OBJECT_PATH = "/storage/v1/object/";
    // Đường dẫn truy cập công khai tới object.
    public static final String STORAGE_PUBLIC_OBJECT_PATH = STORAGE_OBJECT_PATH + "public/";

    private Constant() {
    }
}
