package com.example.producr_service.application.dto.embedding;

import java.util.List;
import java.util.Map;

public record UserPreferenceProfile(
        // vector sơ thích user
        List<Float> vector,
        Map<Long, Double> categoryScores,
        Map<Long, Double> brandScores,
        double preferredPrice) {

    public UserPreferenceProfile {
        vector = List.copyOf(vector);
        categoryScores = Map.copyOf(categoryScores);
        brandScores = Map.copyOf(brandScores);
    }

    public boolean hasSignals() {
        return !vector.isEmpty() || !categoryScores.isEmpty()
                || !brandScores.isEmpty() || preferredPrice > 0;
    }
}
