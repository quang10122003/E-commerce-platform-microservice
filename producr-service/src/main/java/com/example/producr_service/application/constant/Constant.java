package com.example.producr_service.application.constant;

import com.example.producr_service.domain.model.BehaviorType;

import java.time.Duration;
import java.util.Map;

public final class Constant {
    public static final int TOP_SELLING_PRODUCTS_LIMIT = 50;
    public static final int PRODUCT_VIEW_DELAY_SECONDS = 3;
    public static final int BEHAVIOR_EVENT_ID_LENGTH = 36;
    public static final String BEHAVIOR_EVENT_ID_PATTERN =
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    public static final String PRODUCT = "Product";
    public static final String VARIANT = "Variant";
    // toppic của producr service
    public static final String PRODUCT_EVENTS_TOPIC = "product-service.events";
    public static final String ORDER_EVENTS_TOPIC = "order-service.events";
    public static final String DEAD_LETTER_TOPIC = "product-service.DLT";
    // Giới hạn sinh SKU theo cột lưu trữ và số lần thử tránh trùng.
    public static final int MAX_SKU_LENGTH = 50;
    public static final int MAX_SKU_RETRY = 10;
    public static final int SKU_RANDOM_SUFFIX_LENGTH = 4;
    // Tên index trên Elasticsearch.
    public static final String SEARCH_INDEX = "product_search";
    public static final String PRODUCT_EMBEDDING_INDEX = "product_embedding";

    public static final int PRODUCT_EMBEDDING_READ_BATCH_SIZE = 200;

    // Kích thước chiều vector.
    public static final int PRODUCT_EMBEDDING_DIMENSIONS = 768;
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


    public static final int RECOMMENDATION_CACHE_LIMIT = 500;
    public static final int RECOMMENDATION_VECTOR_CANDIDATE_LIMIT = 500;
    public static final int RECOMMENDATION_RULE_CANDIDATE_LIMIT = 100;

    public static final Duration RECOMMENDATION_CACHE_TTL =
            Duration.ofHours(1);

    // thời gian điểm giảm 1 nửa để tính sở thích
    public static final double PREFERENCE_HALF_LIFE_DAYS = 7.0;

    // trong số hành vi — dùng để tính sở thích user
    public static final Map<BehaviorType, Double> PREFERENCE_BEHAVIOR_WEIGHTS =
            Map.of(
                    BehaviorType.PRODUCT_VIEW, 1.0,
                    BehaviorType.ADD_TO_CART, 3.0,
                    BehaviorType.PURCHASE, 5.0);
//    Trọng số xếp hạng
    public static final double RECOMMENDATION_COSINE_WEIGHT = 0.50;
    public static final double RECOMMENDATION_CATEGORY_WEIGHT = 0.20;
    public static final double RECOMMENDATION_BRAND_WEIGHT = 0.10;
    public static final double RECOMMENDATION_PRICE_WEIGHT = 0.10;
    public static final double RECOMMENDATION_POPULARITY_WEIGHT = 0.10;


    private Constant() {
    }
}
