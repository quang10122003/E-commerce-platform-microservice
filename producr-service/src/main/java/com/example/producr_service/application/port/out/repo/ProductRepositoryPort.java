package com.example.producr_service.application.port.out.repo;

import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.request.SellerProductFilter;
import com.example.producr_service.application.port.out.storage.StoredFile;
import com.example.producr_service.domain.model.Product;

import java.util.Optional;
import java.util.List;

public interface ProductRepositoryPort {
    // Đọc toàn bộ lựa chọn sản phẩm thuộc Shop, gồm cả sản phẩm bị khóa.
    List<Product> findAllByUserId(Long userId);

    Product save(Product product);
    Product update(Product product);
    Optional<Product> findById(Long id);

    PageResponse<Product> findProductsFillter(
            Long sellerUserId,
            SellerProductFilter filter
    );
    // Khóa sản phẩm và lấy ID người sở hữu.
    Optional<Long> findOwnerIdByIdForUpdate(Long productId);
    void deleteById(Long productId);

    // Lấy thông tin ảnh bìa của sản phẩm trước khi xóa.
    StoredFile findCoverFile(Long productId);

}
