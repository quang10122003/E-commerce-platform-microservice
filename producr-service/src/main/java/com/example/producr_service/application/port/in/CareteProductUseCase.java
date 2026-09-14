package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.request.CreateProductCommand;
import com.example.producr_service.application.dto.response.ProductResponse;

public interface CareteProductUseCase {
    ProductResponse createProduct(CreateProductCommand command);
}
