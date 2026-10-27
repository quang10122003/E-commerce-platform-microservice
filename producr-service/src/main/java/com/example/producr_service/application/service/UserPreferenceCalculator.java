package com.example.producr_service.application.service;

import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.application.dto.embedding.ProductEmbeddingData;
import com.example.producr_service.application.dto.embedding.ProductPreferenceData;
import com.example.producr_service.application.dto.embedding.UserPreferenceProfile;
import com.example.producr_service.application.dto.response.ProductSearchResponse;
import com.example.producr_service.domain.model.UserProductBehavior;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserPreferenceCalculator {

    // Hành vi mạnh và gần đây đóng góp nhiều hơn vào vector, sở thích danh mục, thương hiệu và mức giá.
    public UserPreferenceProfile buildProfile(
            List<UserProductBehavior> behaviors,
            Map<Long, ProductPreferenceData> products,
            Map<Long, ProductEmbeddingData> embeddings,
            Instant now) {
        Map<Long, Double> categories = new HashMap<>();
        Map<Long, Double> brands = new HashMap<>();
//        Mảng cộng dồn vector sản phẩm × trọng số hành vi
        double[] vectorSum = new double[Constant.PRODUCT_EMBEDDING_DIMENSIONS];
//        Tổng trọng số của các hành vi có vector hợp lệ
        double vectorWeight = 0;
//        Tổng giá sản phẩm × trọng số hành vi
        double priceSum = 0;
//        Tổng trọng số của các hành vi có giá hợp lệ, lớn hơn 0
        double priceWeight = 0;

        for (UserProductBehavior behavior : behaviors) {
            double weight = behaviorWeight(behavior, now);
            ProductPreferenceData product = products.get(behavior.getProductId());
            if (weight <= 0 || product == null) {
                continue;
            }
            if (product.categoryId() != null) {
                categories.merge(product.categoryId(), weight, Double::sum);
            }
            if (product.brandId() != null) {
                brands.merge(product.brandId(), weight, Double::sum);
            }
            double price = positivePrice(product.price());
            if (price > 0) {
                priceSum += weight * price;
                priceWeight += weight;
            }
            // Thiếu vector chỉ bỏ đóng góp embedding, vẫn giữ tín hiệu danh mục, thương hiệu và giá ở trên.
            ProductEmbeddingData embedding = embeddings.get(product.productId());
            if (embedding == null || vectorNorm(embedding.vector()) == 0) {
                continue;
            }
            for (int index = 0; index < vectorSum.length; index++) {
                vectorSum[index] += weight * embedding.vector().get(index);
            }
            vectorWeight += weight;
        }
        return new UserPreferenceProfile(normalize(vectorSum, vectorWeight), categories, brands,
                priceWeight > 0 ? priceSum / priceWeight : 0);
    }
//    chấm điểm các sản phẩm, sắp xếp rồi trả danh sách ID theo thứ tự gợi ý
    public List<Long> rank(
            UserPreferenceProfile profile,
            List<ProductSearchResponse> products,
            Map<Long, ProductEmbeddingData> embeddings) {
        // lấy điểm cao nhất trong từng map sở thích
        double maxCategory = maximum(profile.categoryScores());
        double maxBrand = maximum(profile.brandScores());
        long maxSold = products.stream().mapToLong(ProductSearchResponse::totalSold).max().orElse(0);

        return products.stream()
                .map(product -> new RankedProduct(product,
                        score(profile, product, embeddings.get(product.id()), maxCategory, maxBrand, maxSold)))
                .sorted(Comparator.comparingDouble(RankedProduct::score).reversed()
                        .thenComparing(Comparator.comparingLong(
                                (RankedProduct ranked) -> ranked.product().totalSold()).reversed())
                        .thenComparing(ranked -> ranked.product().id(), Comparator.reverseOrder()))
                .limit(Constant.RECOMMENDATION_CACHE_LIMIT)
                .map(ranked -> ranked.product().id())
                .toList();
    }

    private double behaviorWeight(UserProductBehavior behavior, Instant now) {
        // tính hành vi xảy ra bao lâu
        Duration age = Duration.between(behavior.getCreatedAt(), now);
        if (age.isNegative() || age.compareTo(UserProductBehavior.RETENTION) >= 0) {
            return 0;
        }
        double ageDays = age.toNanos() / (double) Duration.ofDays(1).toNanos();
        // lấy điểm theo trọng số gốc rùi giảm 1 nửa theo PREFERENCE_HALF_LIFE_DAYS
        return Constant.PREFERENCE_BEHAVIOR_WEIGHTS.getOrDefault(behavior.getEventType(), 0.0)
                * Math.pow(0.5, ageDays / Constant.PREFERENCE_HALF_LIFE_DAYS);
    }

    private List<Float> normalize(double[] vectorSum, double totalWeight) {
        if (totalWeight <= 0) {
            return List.of();
        }
        double squaredNorm = 0;
        for (int index = 0; index < vectorSum.length; index++) {
            vectorSum[index] /= totalWeight;
            squaredNorm += vectorSum[index] * vectorSum[index];
        }
        if (squaredNorm <= 0 || !Double.isFinite(squaredNorm)) {
            return List.of();
        }
        double norm = Math.sqrt(squaredNorm);
        List<Float> vector = new ArrayList<>(vectorSum.length);
        for (double value : vectorSum) {
            vector.add((float) (value / norm));
        }
        return vector;
    }
//    tính một điểm tổng cho một sản phẩm, để biết sản phẩm đó phù hợp với user đến mức nào
    private double score(UserPreferenceProfile profile, ProductSearchResponse product,
                         ProductEmbeddingData embedding, double maxCategory, double maxBrand, long maxSold) {
        double category = affinity(profile.categoryScores(), product.categoryId(), maxCategory);
        double brand = affinity(profile.brandScores(), product.brandId(), maxBrand);
        double price = positivePrice(product.maxPrice());
        double priceScore = price > 0 && profile.preferredPrice() > 0
                ? Math.exp(-Math.abs(Math.log(price / profile.preferredPrice()))) : 0;

        double popularity = maxSold > 0
                ? Math.log1p(Math.max(0, product.totalSold())) / Math.log1p(maxSold) : 0;

        double rules = Constant.RECOMMENDATION_CATEGORY_WEIGHT * category
                + Constant.RECOMMENDATION_BRAND_WEIGHT * brand
                + Constant.RECOMMENDATION_PRICE_WEIGHT * priceScore
                + Constant.RECOMMENDATION_POPULARITY_WEIGHT * popularity;

        Double cosine = cosine(profile.vector(), embedding);
//        ách tính điểm khi không có vector hợp lệ để tính cosine chia có ruleWeight để trọng số ko bị giới hạn = rule khi k có cosine
        if (cosine == null) {
            double ruleWeight = Constant.RECOMMENDATION_CATEGORY_WEIGHT + Constant.RECOMMENDATION_BRAND_WEIGHT
                    + Constant.RECOMMENDATION_PRICE_WEIGHT + Constant.RECOMMENDATION_POPULARITY_WEIGHT;
            return ruleWeight > 0 ? rules / ruleWeight : 0;
        }
        return Constant.RECOMMENDATION_COSINE_WEIGHT * cosine + rules;
    }

    // tính chỉ số cosine để tính độ tương đồ 2 vector
    private Double cosine(List<Float> userVector, ProductEmbeddingData embedding) {
        if (embedding == null) {
            return null;
        }
        double userNorm = vectorNorm(userVector);
        double productNorm = vectorNorm(embedding.vector());
        if (userNorm == 0 || productNorm == 0) {
            return null;
        }
        double dot = 0;
        for (int index = 0; index < userVector.size(); index++) {
            dot += (double) userVector.get(index) * embedding.vector().get(index);
        }
        return Math.max(0, Math.min(1, dot / (userNorm * productNorm)));
    }

    private double vectorNorm(List<Float> vector) {
        if (vector == null || vector.size() != Constant.PRODUCT_EMBEDDING_DIMENSIONS) {
            return 0;
        }
        double squaredNorm = 0;
        for (Float value : vector) {
            if (value == null || !Float.isFinite(value)) {
                return 0;
            }
            squaredNorm += (double) value * value;
        }
        return Double.isFinite(squaredNorm) ? Math.sqrt(squaredNorm) : 0;
    }

    private double maximum(Map<Long, Double> scores) {
        return scores.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
    }

    private double affinity(Map<Long, Double> scores, Long id, double max) {
        return id != null && max > 0 ? scores.getOrDefault(id, 0.0) / max : 0;
    }

    private double positivePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            return 0;
        }
        double value = price.doubleValue();
        return Double.isFinite(value) ? value : 0;
    }

    private record RankedProduct(ProductSearchResponse product, double score) {}
}
