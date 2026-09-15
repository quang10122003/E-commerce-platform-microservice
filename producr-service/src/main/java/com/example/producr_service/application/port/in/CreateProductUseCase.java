package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.command.CreateProductCommand;
import com.example.producr_service.application.dto.response.ProductResponse;

public interface CreateProductUseCase {
    ProductResponse createProduct(CreateProductCommand command);
}
