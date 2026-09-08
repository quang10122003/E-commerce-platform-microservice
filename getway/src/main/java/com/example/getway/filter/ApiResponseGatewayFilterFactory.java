package com.example.getway.filter;

import com.example.common.response.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

/**
 * Bọc response JSON thành cấu trúc ApiResponse chuẩn trước khi trả về client.
 */
@Component
public class ApiResponseGatewayFilterFactory
        extends AbstractGatewayFilterFactory<ApiResponseGatewayFilterFactory.Config> {

    private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilterFactory;
    private final ObjectMapper objectMapper;

    public ApiResponseGatewayFilterFactory(
            ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilterFactory,
            ObjectMapper objectMapper) {
        super(Config.class);
        this.modifyResponseBodyFilterFactory = modifyResponseBodyFilterFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        // tapk filter để getway sử dụng
        ModifyResponseBodyGatewayFilterFactory.Config rewriteConfig =
                new ModifyResponseBodyGatewayFilterFactory.Config();

        rewriteConfig.setRewriteFunction(String.class, String.class, (exchange, body) -> {
            HttpStatusCode status = exchange.getResponse().getStatusCode();
            if (body == null || body.isBlank() || status == null || !status.is2xxSuccessful()) {
                return Mono.justOrEmpty(body);
            }

            try {
                JsonNode data = objectMapper.readTree(body);
                if (isApiResponse(data)) {
                    return Mono.just(body);
                }

                return Mono.just(objectMapper.writeValueAsString(
                        ApiResponse.success("Thành công", data)));
            } catch (Exception exception) {
                return Mono.just(body);
            }
        });

        return modifyResponseBodyFilterFactory.apply(rewriteConfig);
    }

    private boolean isApiResponse(JsonNode body) {
        return body.isObject()
                && body.has("success")
                && body.has("data")
                && body.has("timestamp");
    }

    public static class Config {
    }
}
