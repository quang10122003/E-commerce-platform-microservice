package com.example.producr_service.adapter.in.rest;

import com.example.common.response.PageResponse;
import com.example.producr_service.adapter.DTO.request.CreateCategoryRequest;
import com.example.producr_service.adapter.mapper.ProductMultipartCommandMapper;
import com.example.producr_service.application.dto.response.AdminCategoryResponse;
import com.example.producr_service.application.dto.response.CategoryResponse;
import com.example.producr_service.application.port.in.CreateCategoryUseCase;
import com.example.producr_service.application.port.in.DeleteCategoryUseCase;
import com.example.producr_service.application.port.in.UpdateCategoryUseCase;
import com.example.producr_service.application.port.in.GetAdminCategoriesUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminCategoryController {
    private final CreateCategoryUseCase createCategoryUseCase;
    private final GetAdminCategoriesUseCase getAdminCategoriesUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;
    private final ProductMultipartCommandMapper multipartCommandMapper;

    @GetMapping
    public ResponseEntity<PageResponse<AdminCategoryResponse>> getCategories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(getAdminCategoriesUseCase.getAdminCategories(page, size, keyword));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestPart("request") @Valid CreateCategoryRequest request,
            @RequestPart("image") MultipartFile image
    ) throws IOException {
        CategoryResponse response = createCategoryUseCase.createCategory(
                request.getName(), multipartCommandMapper.toUploadFileCommand(image));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{categoryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable @Positive Long categoryId,
            @RequestPart("request") @Valid CreateCategoryRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        CategoryResponse response = updateCategoryUseCase.updateCategory(categoryId, request.getName(),
                image == null || image.isEmpty() ? null : multipartCommandMapper.toUploadFileCommand(image));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable @Positive Long categoryId) {
        deleteCategoryUseCase.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
