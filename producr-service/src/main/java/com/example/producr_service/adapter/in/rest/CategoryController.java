package com.example.producr_service.adapter.in.rest;

import com.example.producr_service.application.dto.response.CategoryResponse;
import com.example.producr_service.application.port.in.GetCategoriesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/categories")
public class CategoryController {
    // Use case phục vụ thao tác lấy danh sách category.
    private final GetCategoriesUseCase getCategoriesUseCase;

    // Trả về danh sách category cho client.
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(getCategoriesUseCase.getCategories());
    }
}
