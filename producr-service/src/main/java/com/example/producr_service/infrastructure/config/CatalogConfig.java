package com.example.producr_service.infrastructure.config;

import com.example.producr_service.application.port.in.DeleteBrandUseCase;
import com.example.producr_service.application.port.in.GetAdminBrandsUseCase;
import com.example.producr_service.application.port.in.CreateCategoryUseCase;
import com.example.producr_service.application.port.in.DeleteCategoryUseCase;
import com.example.producr_service.application.port.in.UpdateCategoryUseCase;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.in.GetAdminCategoriesUseCase;
import com.example.producr_service.application.port.in.GetCategoriesUseCase;
import com.example.producr_service.application.port.in.GetBrandsUseCase;
import com.example.producr_service.application.port.out.repo.BrandRepositoryPort;
import com.example.producr_service.application.port.out.repo.CategoryRepoPort;
import com.example.producr_service.application.port.out.storage.StoredFileRollbackPort;
import com.example.producr_service.application.service.CategoryService;
import com.example.producr_service.application.service.FileUploader;
import com.example.producr_service.application.service.BrandService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class CatalogConfig {

    @Bean
    CategoryService categoryService(CategoryRepoPort categoryRepoPort, ProductRepositoryPort productRepositoryPort,
                                    FileUploader fileUploader, StoredFileRollbackPort storedFileRollbackPort,
                                    OutboxPort outboxPort, Clock clock) {
        return new CategoryService(categoryRepoPort, productRepositoryPort, fileUploader, storedFileRollbackPort,
                outboxPort, clock);
    }

    @Bean
    GetCategoriesUseCase getCategoriesUseCase(CategoryService categoryService) {
        return categoryService;
    }

    @Bean
    GetAdminCategoriesUseCase getAdminCategoriesUseCase(CategoryService categoryService) {
        return categoryService;
    }

    @Bean
    UpdateCategoryUseCase updateCategoryUseCase(CategoryService categoryService) {
        return categoryService;
    }

    @Bean
    DeleteCategoryUseCase deleteCategoryUseCase(CategoryService categoryService) {
        return categoryService;
    }

    @Bean
    CreateCategoryUseCase createCategoryUseCase(CategoryService categoryService) {
        return categoryService;
    }

    @Bean
    BrandService brandService(BrandRepositoryPort brandRepositoryPort, ProductRepositoryPort productRepositoryPort,
                              OutboxPort outboxPort, Clock clock) {
        return new BrandService(brandRepositoryPort, productRepositoryPort, outboxPort, clock);
    }

    @Bean
    DeleteBrandUseCase deleteBrandUseCase(BrandService brandService) {
        return brandService;
    }

    @Bean
    GetBrandsUseCase getBrandsUseCase(BrandService brandService) {
        return brandService;
    }

    @Bean
    GetAdminBrandsUseCase getAdminBrandsUseCase(BrandService brandService) {
        return brandService;
    }
}
