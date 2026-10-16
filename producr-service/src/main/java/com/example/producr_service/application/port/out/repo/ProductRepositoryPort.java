package com.example.producr_service.application.port.out.repo;

import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.request.SellerProductFilter;
import com.example.producr_service.application.dto.response.ProductSummaryResponse;
import com.example.producr_service.application.port.out.storage.StoredFile;
import com.example.producr_service.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {
    List<ProductSummaryResponse> findProductSummariesByIds(List<Long> productIds);
    // Đọc toàn bộ lựa chọn sản phẩm thuộc Shop, gồm cả sản phẩm bị khóa.
    List<Product> findAllByUserId(Long userId);

    Product save(Product product);
    Product update(Product product);
    Optional<Product> findById(Long id);

    // Kiểm tra bản ghi sản phẩm tồn tại, không lọc trạng thái hoặc tải aggregate.
    boolean existsById(Long productId);

    // Đọc ID người tạo sản phẩm, không khóa hoặc tải aggregate.
    Optional<Long> findOwnerIdById(Long productId);

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
