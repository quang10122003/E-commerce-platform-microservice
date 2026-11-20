package com.example.producr_service.application.service;


import com.example.common.exception.BusinessException;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.application.dto.command.UploadFileCommand;
import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.dto.outbox.StoredFilesDeleteOutboxPayload;
import com.example.producr_service.application.dto.response.AdminCategoryResponse;
import com.example.producr_service.application.dto.response.AdminCategoryView;
import com.example.producr_service.application.dto.response.CategoryResponse;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.in.CreateCategoryUseCase;
import com.example.producr_service.application.port.in.DeleteCategoryUseCase;
import com.example.producr_service.application.port.in.GetAdminCategoriesUseCase;
import com.example.producr_service.application.port.in.GetCategoriesUseCase;
import com.example.producr_service.application.port.in.UpdateCategoryUseCase;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import com.example.producr_service.application.port.out.repo.CategoryRepoPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.storage.StorageBucket;
import com.example.producr_service.application.port.out.storage.StoredFile;
import com.example.producr_service.application.port.out.storage.StoredFileRollbackPort;
import com.example.producr_service.application.strategy.upload.UploadPurpose;
import com.example.producr_service.domain.model.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class CategoryService implements GetCategoriesUseCase, CreateCategoryUseCase, GetAdminCategoriesUseCase,
        UpdateCategoryUseCase, DeleteCategoryUseCase {
    private final CategoryRepoPort categoryRepoPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final FileUploader fileUploader;
    private final StoredFileRollbackPort storedFileRollbackPort;
    private final OutboxPort outboxPort;
    private final Clock clock;

    @Transactional(readOnly = true)
    @Override
    public List<CategoryResponse> getCategories() {
        List<Category> categories = categoryRepoPort.getAllCategories();
        Map<Long, Long> productCounts = productRepositoryPort.countProductsByCategoryIds(
                categories.stream().map(Category::getId).toList());
        return categories
                .stream()
                .map(category -> CategoryResponse.from(category, productCounts.getOrDefault(category.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<AdminCategoryResponse> getAdminCategories(int page, int size, String keyword) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BusinessException(ProductError.INVALID_PAGINATION);
        }
        PageResponse<AdminCategoryView> categories = categoryRepoPort.findAdminCategories(
                new PageQuery(page, size), keyword == null || keyword.isBlank() ? null : keyword.trim());
        Map<Long, Long> productCounts = productRepositoryPort.countProductsByCategoryIds(
                categories.items().stream().map(AdminCategoryView::id).toList());
        return new PageResponse<>(
                categories.items().stream()
                        .map(category -> AdminCategoryResponse.from(category, clock.getZone(),
                                productCounts.getOrDefault(category.id(), 0L)))
                        .toList(),
                categories.page(),
                categories.pageSize(),
                categories.totalItems(),
                categories.totalPages()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CategoryResponse createCategory(String name, UploadFileCommand image) {
        String categoryName = name.trim();
        // Kiểm tra trùng tên trước để không upload ảnh thừa.
        if (categoryRepoPort.existsByName(categoryName)) {
            throw new BusinessException(ProductError.CATEGORY_NAME_DUPLICATED);
        }

        List<StoredFile> storedFiles = new ArrayList<>();
        // Đăng ký trước khi upload để file đã lưu được dọn nếu transaction rollback.
        storedFileRollbackPort.deleteImageStore(storedFiles);
        StoredFile storedFile = fileUploader.upload(UploadPurpose.CATEGORY_IMAGE, image);
        storedFiles.add(storedFile);

        Category category = categoryRepoPort.save(new Category(
                null, categoryName, storedFile.publicUrl(), storedFile.objectPath()));
        log.info("Tạo danh mục thành công categoryId={}", category.getId());
        return CategoryResponse.from(category);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CategoryResponse updateCategory(Long categoryId, String name, UploadFileCommand image) {
        // Khóa danh mục để hai lần sửa đồng thời không cùng dọn một ảnh cũ và bỏ sót ảnh mới.
        Category category = categoryRepoPort.findByIdForUpdate(categoryId)
                .orElseThrow(() -> new BusinessException(ProductError.CATEGORY_NOT_FOUND));
        String categoryName = name.trim();
        if (categoryRepoPort.existsByNameAndIdNot(categoryName, categoryId)) {
            throw new BusinessException(ProductError.CATEGORY_NAME_DUPLICATED);
        }
        boolean renamed = !category.getName().equals(categoryName);
        category.rename(categoryName);

        if (image != null) {
            String oldObjectPath = category.getObjectPath();
            String oldImageUrl = category.getImageUrl();
            List<StoredFile> storedFiles = new ArrayList<>();
            // Đăng ký trước khi upload để ảnh mới được dọn nếu transaction rollback.
            storedFileRollbackPort.deleteImageStore(storedFiles);
            StoredFile storedFile = fileUploader.upload(UploadPurpose.CATEGORY_IMAGE, image);
            storedFiles.add(storedFile);
            category.replaceImage(storedFile.publicUrl(), storedFile.objectPath());
            // Ảnh cũ chỉ bị xóa sau commit qua outbox; danh mục cũ chưa có object_path thì không dọn được.
            if (oldObjectPath != null) {
                saveDeleteImageEvent(categoryId, new StoredFile(StorageBucket.CATEGORY, oldObjectPath, oldImageUrl));
            }
        }

        Category saved = categoryRepoPort.update(category);
        if (renamed) {
            productRepositoryPort.findIdsByCategoryId(categoryId)
                    .forEach(productId -> ProductSyncEvents.save(outboxPort, clock, productId));
        }
        log.info("Cập nhật danh mục thành công categoryId={}, renamed={}, imageChanged={}",
                categoryId, renamed, image != null);
        return CategoryResponse.from(saved);
    }

    @Transactional
    @Override
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepoPort.findByIdForUpdate(categoryId)
                .orElseThrow(() -> new BusinessException(ProductError.CATEGORY_NOT_FOUND));
        // FK RESTRICT vẫn chặn ở DB; kiểm tra trước để trả lỗi nghiệp vụ rõ ràng.
        if (productRepositoryPort.existsByCategoryId(categoryId)) {
            throw new BusinessException(ProductError.CATEGORY_HAS_PRODUCTS);
        }
        if (category.getObjectPath() != null) {
            saveDeleteImageEvent(categoryId,
                    new StoredFile(StorageBucket.CATEGORY, category.getObjectPath(), category.getImageUrl()));
        }
        categoryRepoPort.deleteById(categoryId);
        log.info("Xóa danh mục thành công categoryId={}", categoryId);
    }

    private void saveDeleteImageEvent(Long categoryId, StoredFile file) {
        outboxPort.save(new OutboxEventDto(UUID.randomUUID(), Constant.CATEGORY, String.valueOf(categoryId),
                StoredFilesDeleteOutboxPayload.EVENT_TYPE, new StoredFilesDeleteOutboxPayload(List.of(file)),
                LocalDateTime.now(clock)));
    }

}
