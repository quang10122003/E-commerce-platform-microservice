package com.example.producr_service.infrastructure.config;

import com.example.producr_service.application.port.in.GetCategoriesUseCase;
import com.example.producr_service.application.port.in.GetBrandsUseCase;
import com.example.producr_service.application.port.out.repo.BrandRepositoryPort;
import com.example.producr_service.application.port.out.repo.CategoryRepoPort;
import com.example.producr_service.application.service.CategoryService;
import com.example.producr_service.application.service.BrandService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogConfig {

    // Đăng ký service lấy category theo mô hình port in/out của ứng dụng.
    @Bean
    CategoryService categoryService(CategoryRepoPort categoryRepoPort) {
        return new CategoryService(categoryRepoPort);
    }

    // Cung cấp use case lấy category cho adapter vào như controller.
    @Bean
    GetCategoriesUseCase getCategoriesUseCase(CategoryService categoryService) {
        return categoryService;
    }

    // Tạo bean theo port để controller không phụ thuộc implementation của service.
    @Bean
    GetBrandsUseCase getBrandsUseCase(BrandRepositoryPort brandRepositoryPort) {
        return new BrandService(brandRepositoryPort);
    }
}
