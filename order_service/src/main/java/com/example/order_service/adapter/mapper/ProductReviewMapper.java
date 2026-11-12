package com.example.order_service.adapter.mapper;

import com.example.order_service.adapter.entity.ProductReviewEntity;
import com.example.order_service.adapter.entity.ProductReviewImageEntity;
import com.example.order_service.adapter.out.persistence.projection.ReviewableOrderItemProjection;
import com.example.order_service.application.dto.review.ReviewableOrderItemView;
import com.example.order_service.domain.model.ProductReview;
import com.example.order_service.domain.model.ReviewImage;
import org.springframework.stereotype.Component;

@Component
public class ProductReviewMapper {
    public ReviewableOrderItemView toView(ReviewableOrderItemProjection item) {
        return new ReviewableOrderItemView(item.getOrderItemId(), item.getOrderId(), item.getProductId(),
                item.getShopId(), item.getBuyerId(), item.getBuyerName(), item.getVariantAttributes(),
                item.getOrderStatus(), item.getDeliveredAt());
    }

    public ProductReviewEntity toEntity(ProductReview review) {
        ProductReviewEntity entity = new ProductReviewEntity();
        entity.setOrderItemId(review.getOrderItemId());
        entity.setOrderId(review.getOrderId());
        entity.setProductId(review.getProductId());
        entity.setShopId(review.getShopId());
        entity.setUserId(review.getUserId());
        entity.setReviewerNameSnapshot(review.getReviewerNameSnapshot());
        entity.setVariantAttributes(review.getVariantAttributes());
        entity.setRating(review.getRating());
        entity.setContent(review.getContent());
        entity.setCreatedAt(review.getCreatedAt());
        for (ReviewImage image : review.getImages()) {
            ProductReviewImageEntity imageEntity = new ProductReviewImageEntity();
            imageEntity.setReview(entity);
            imageEntity.setImageUrl(image.getImageUrl());
            imageEntity.setObjectPath(image.getObjectPath());
            imageEntity.setStorageBucket(image.getStorageBucket());
            imageEntity.setSortOrder(image.getSortOrder());
            entity.getImages().add(imageEntity);
        }
        return entity;
    }

    public ProductReview toDomain(ProductReviewEntity entity) {
        return ProductReview.reconstitute(entity.getId(), entity.getOrderItemId(), entity.getOrderId(),
                entity.getProductId(), entity.getShopId(), entity.getUserId(), entity.getReviewerNameSnapshot(),
                entity.getVariantAttributes(), entity.getRating(), entity.getContent(),
                entity.getImages().stream()
                        .map((ProductReviewImageEntity image) -> new ReviewImage(image.getId(), image.getImageUrl(),
                                image.getObjectPath(), image.getStorageBucket(), image.getSortOrder()))
                        .toList(),
                entity.getCreatedAt());
    }
}
