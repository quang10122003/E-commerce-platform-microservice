package com.example.producr_service.adapter.out.elasticsearch;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.example.producr_service.adapter.DTO.documentElasticsearch.ProductSearchDocument;
import com.example.producr_service.adapter.DTO.documentElasticsearch.ProductSearchDocumentFields;
import com.example.producr_service.adapter.mapper.ProductSearchDocumentMapper;
import com.example.producr_service.application.dto.request.ProductScrollFilter;
import com.example.producr_service.application.dto.request.ProductSortOption;
import com.example.producr_service.application.dto.response.ProductBrandOption;
import com.example.producr_service.application.dto.response.ProductCatalogSearchResponse;
import com.example.producr_service.application.dto.response.ProductSearchResponse;
import com.example.producr_service.application.port.out.ES.ProductSearchIndexPort;
import com.example.producr_service.application.port.out.ES.ProductSearchPort;
import com.example.producr_service.application.port.out.repo.BrandRepositoryPort;
import com.example.producr_service.application.port.out.repo.CategoryRepoPort;
import com.example.producr_service.domain.model.Brand;
import com.example.producr_service.domain.model.Category;
import com.example.producr_service.domain.model.Product;
import com.example.producr_service.domain.model.ProductStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Objects;

import static com.example.common.untill.ValidationUtils.hasText;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class ProductElasticsearchAdapter implements ProductSearchIndexPort, ProductSearchPort {

    private static final String BRANDS_AGGREGATION = "matched_brands";

    // Chuyển aggregate Product sang document dành cho Elasticsearch.
    ProductSearchDocumentMapper productSearchDocumentMapper;

    // Thực hiện thao tác lưu document vào Elasticsearch.
    ProductSearchRepository productSearchRepository;

    ElasticsearchOperations elasticsearchOperations;

    ProductSearchCursorCodec productSearchCursorCodec;

    CategoryRepoPort categoryRepoPort;
    BrandRepositoryPort brandRepositoryPort;

    // hàm tạo or thêm document cho product
    @Override
    public void index(Product product, String location) {
        String categoryName = findCategoryName(product.getCategoryId());
        String brandName = findBrandName(product.getBrandId());

        ProductSearchDocument document =
                productSearchDocumentMapper.toDocument(
                        product,
                        categoryName,
                        brandName,
                        location
                );

        productSearchRepository.save(document);
    }

    // Tìm kiếm catalog product bằng cursor để hỗ trợ infinity scroll.
    @Override
    public ProductCatalogSearchResponse search(ProductScrollFilter filter) {
        // Lấy dư một item để xác định còn batch kế tiếp hay không.
        Pageable pageable = PageRequest.of(
                0,
                filter.size() + 1
        );

        var queryBuilder = NativeQuery.builder()
                .withQuery(boolQueryBuilder -> boolQueryBuilder.bool(bool -> {
                    // Tìm theo tên product khi client truyền keyword.
                    if (hasText(filter.keyword())) {
                        String keyword = filter.keyword().trim();
                        bool.must(multiMatch -> multiMatch.multiMatch(multiMatchQuery -> multiMatchQuery
                                .query(keyword)
                                .type(TextQueryType.BoolPrefix)
                                .operator(Operator.And)
                                .fields(
                                        ProductSearchDocumentFields.NAME + "^5",
                                        ProductSearchDocumentFields.BRAND_NAME + "^2",
                                        ProductSearchDocumentFields.CATEGORY_NAME
                                )
                        ));

                        // Ưu tiên product có tên khớp đúng cụm keyword người dùng nhập.
                        bool.should(matchPhrase -> matchPhrase.matchPhrase(matchPhraseQuery -> matchPhraseQuery
                                .field(ProductSearchDocumentFields.NAME)
                                .query(keyword)
                                .boost(8.0f)
                        ));
                    }

                    // Lọc theo category khi client truyền categoryId.
                    if (filter.categoryId() != null) {
                        bool.filter(term -> term.term(termQuery -> termQuery
                                .field(ProductSearchDocumentFields.CATEGORY_ID)
                                .value(filter.categoryId())
                        ));
                    }

                    // So sánh giá lọc với maxPrice của mỗi product.
                    if (filter.minPrice() != null) {
                        bool.filter(range -> range.range(rangeQuery -> rangeQuery
                                .number(numberRange -> numberRange
                                        .field(ProductSearchDocumentFields.MAX_PRICE)
                                        .gte(filter.minPrice().doubleValue())
                                )
                        ));
                    }

                    // Giới hạn giá tối đa dựa trên maxPrice của mỗi product.
                    if (filter.maxPrice() != null) {
                        bool.filter(range -> range.range(rangeQuery -> rangeQuery
                                .number(numberRange -> numberRange
                                        .field(ProductSearchDocumentFields.MAX_PRICE)
                                        .lte(filter.maxPrice().doubleValue())
                                )
                        ));
                    }

                    // Chỉ hiển thị product đang hoạt động cho shop.
                    bool.filter(term -> term.term(termQuery -> termQuery
                            .field(ProductSearchDocumentFields.STATUS + ".keyword")
                            .value(ProductStatus.ACTIVE.name())
                    ));

                    // filter theo tính thành
                    if (!filter.locations().isEmpty()) {
                        bool.filter(terms -> terms.terms(termsQuery -> termsQuery
                                .field(ProductSearchDocumentFields.LOCATION)
                                .terms(values -> values.value(filter.locations().stream()
                                        .map(FieldValue::of)
                                        .toList()))
                        ));
                    }

                    return bool;
                }))
                ;

        // Lọc hit theo danh sách brand exact nhưng vẫn giữ aggregation đủ brand khớp keyword.
        if (!filter.brandIds().isEmpty()) {
            queryBuilder.withFilter(terms -> terms.terms(termsQuery -> termsQuery
                    .field(ProductSearchDocumentFields.BRAND_ID)
                    .terms(values -> values.value(filter.brandIds().stream()
                            .map(FieldValue::of)
                            .toList()))
            ));
        }

        // Chọn thứ tự ưu tiên theo lựa chọn sắp xếp của người dùng.
        if (filter.sortOption() == ProductSortOption.MOST_SOLD) {
            queryBuilder.withSort(sort -> sort.field(field -> field
                    .field(ProductSearchDocumentFields.TOTAL_SOLD)
                    .order(SortOrder.Desc)
            ));
        } else {
            queryBuilder.withSort(sort -> sort.field(field -> field
                    .field("_score")
                    .order(SortOrder.Desc)
            ));
        }

        // Dùng productId làm khóa phụ ổn định cho cursor ở mọi chế độ sort.
        queryBuilder.withSort(sort -> sort.field(field -> field
                .field(ProductSearchDocumentFields.PRODUCT_ID)
                .order(SortOrder.Desc)
        ));

        // Gom brandId trên toàn bộ tập product khớp để không phụ thuộc vào kích thước page.
        queryBuilder.withAggregation(
                BRANDS_AGGREGATION,
                Aggregation.of(aggregation -> aggregation.terms(terms -> terms
                        .field(ProductSearchDocumentFields.BRAND_ID)
                        .size(50)
                ))
        );

        NativeQuery query = queryBuilder
                .withPageable(pageable)
                .build();

        // Tiếp tục lấy dữ liệu sau item cuối của batch trước.
        if (hasText(filter.cursor())) {
            query.setSearchAfter(productSearchCursorCodec.decode(
                    filter.cursor(),
                    filter.sortOption()
            ));
        }

        SearchHits<ProductSearchDocument> searchHits = elasticsearchOperations.search(
                query,
                ProductSearchDocument.class
        );

        List<SearchHit<ProductSearchDocument>> hits = searchHits.getSearchHits();
        boolean hasNext = hits.size() > filter.size();
        List<SearchHit<ProductSearchDocument>> visibleHits = hits.stream()
                .limit(filter.size())
                .toList();

        List<ProductSearchResponse> items = visibleHits.stream()
                .map(SearchHit::getContent)
                .map(this::toResponse)
                .toList();

        // Cursor mới được tạo từ giá trị sort của item cuối cùng đã trả về.
        String nextCursor = hasNext
                ? productSearchCursorCodec.encode(
                        visibleHits.getLast().getSortValues(),
                        filter.sortOption()
                )
                : null;

        return new ProductCatalogSearchResponse(
                items,
                nextCursor,
                hasNext,
                extractBrands(searchHits)
        );
    }

    // Chuyển các bucket brandId thành danh sách brand cho response catalog.
    private List<ProductBrandOption> extractBrands(SearchHits<ProductSearchDocument> searchHits) {
        if (!searchHits.hasAggregations()) {
            return List.of();
        }

        ElasticsearchAggregations aggregations =
                (ElasticsearchAggregations) searchHits.getAggregations();
        var aggregation = aggregations.get(BRANDS_AGGREGATION);
        if (aggregation == null || !aggregation.aggregation().getAggregate().isLterms()) {
            return List.of();
        }

        return aggregation.aggregation().getAggregate().lterms().buckets().array().stream()
                .map(bucket -> brandRepositoryPort.findById(bucket.key())
                        .map(brand -> new ProductBrandOption(brand.getId(), brand.getName()))
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }


    // Chuyển Elasticsearch document thành response cho API.
    private ProductSearchResponse toResponse(ProductSearchDocument document) {
        return new ProductSearchResponse(
                document.getId(),
                document.getName(),
                document.getDescription(),
                document.getCategoryId(),
                document.getCategoryName(),
                document.getBrandId(),
                document.getBrandName(),
                document.getStatus(),
                document.getImageUrl(),
                document.getMaxPrice(),
                document.getTotalSold()
        );
    }

    // Lấy tên category bắt buộc để làm giàu product search document.
    private String findCategoryName(Long categoryId) {
        return categoryRepoPort.findById(categoryId)
                .map(Category::getName)
                .orElseThrow(() -> new IllegalStateException(
                        "Category id=" + categoryId + " khong ton tai khi index product"));
    }

    // Lấy tên brand khi product có gắn brand.
    private String findBrandName(Long brandId) {
        if (brandId == null) {
            return null;
        }

        return brandRepositoryPort.findById(brandId)
                .map(Brand::getName)
                .orElse(null);
    }
}
