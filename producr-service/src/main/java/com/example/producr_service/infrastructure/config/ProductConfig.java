package com.example.producr_service.infrastructure.config;

import com.example.producr_service.application.port.out.FileStoragePort;
import com.example.producr_service.application.port.in.CreateProductUseCase;
import com.example.producr_service.application.port.out.BrandRepositoryPort;
import com.example.producr_service.application.port.out.CategoryRepoPort;
import com.example.producr_service.application.port.out.CurrentUserPort;
import com.example.producr_service.application.port.out.ProductRepositoryPort;
import com.example.producr_service.application.registry.UploadStrategyRegistry;
import com.example.producr_service.adapter.client.AuthUserFeignClient;
import com.example.producr_service.adapter.out.openFeign.AuthUserAdapter;
import com.example.producr_service.application.service.FileService;
import com.example.producr_service.application.service.CreateProductApplicationService;
import com.example.producr_service.application.service.ProductService;
import com.example.producr_service.application.strategy.CategoryImageUploadStrategy;
import com.example.producr_service.application.strategy.ProductImageUploadStrategy;
import com.example.producr_service.application.strategy.ProductVariantImageUploadStrategy;
import com.example.producr_service.application.strategy.interfaces.IUploadStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ProductConfig {

    // Đăng ký adapter lấy thông tin user hiện tại từ Auth service qua Feign.
    @Bean
    CurrentUserPort currentUserPort(AuthUserFeignClient authUserFeignClient) {
        return new AuthUserAdapter(authUserFeignClient);
    }

    // Đăng ký service chỉ phụ trách tạo và lưu aggregate Product.
    @Bean
    ProductService productService(
            ProductRepositoryPort productRepositoryPort,
            CategoryRepoPort categoryRepositoryPort,
            BrandRepositoryPort brandRepositoryPort
    ) {
        return new ProductService(
                productRepositoryPort,
                categoryRepositoryPort,
                brandRepositoryPort
        );
    }

    // Đăng ký use case điều phối upload và tạo product cho controller.
    @Bean
CreateProductUseCase createProductUseCase(
        FileService fileService,
        ProductService productService,
        CurrentUserPort currentUserPort
) {
    return new CreateProductApplicationService(
            fileService,
            productService,
            currentUserPort
    );
}

    // Đăng ký strategy upload ảnh category trong infrastructure layer.
    @Bean
    IUploadStrategy categoryImageUploadStrategy() {
        return new CategoryImageUploadStrategy();
    }

    // Đăng ký strategy upload ảnh product trong infrastructure layer.
    @Bean
    IUploadStrategy productImageUploadStrategy() {
        return new ProductImageUploadStrategy();
    }

    // Đăng ký strategy upload ảnh product variant đã có trong application layer.
    @Bean
    IUploadStrategy productVariantImageUploadStrategy() {
        return new ProductVariantImageUploadStrategy();
    }

    // Gom các strategy để chọn đúng logic theo loại bucket.
    @Bean
    UploadStrategyRegistry uploadStrategyRegistry(
            List<IUploadStrategy> strategies
    ) {
        return new UploadStrategyRegistry(strategies);
    }

    // Đăng ký application service và cấp adapter lưu trữ cho service.
    @Bean
    FileService fileService(
            FileStoragePort fileStoragePort,
            UploadStrategyRegistry uploadStrategyRegistry
    ) {
        return new FileService(fileStoragePort, uploadStrategyRegistry);
    }
}
