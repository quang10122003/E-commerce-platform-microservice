package com.example.order_service.application.service;

import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import com.example.order_service.application.port.out.client.CurrentUserPort;
import com.example.order_service.application.port.in.CheckCheckoutAccessUseCase;
import com.example.order_service.application.port.out.repo.CheckoutRepoPort;
import com.example.order_service.domain.model.Checkout;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class CheckoutInternalService implements CheckCheckoutAccessUseCase {

    private final CurrentUserPort currentUserPort;
    private final CheckoutRepoPort checkoutRepoPort;

    @Override
    @Transactional(readOnly = true)
    public void checkAccess(Long checkoutId) {
        Long userId = currentUserPort.getInfo().userId();
        Checkout checkout = checkoutRepoPort.findById(checkoutId)
                .orElseThrow(() -> new BusinessException(AuthorizationError.ACCESS_DENIED));
        if (!checkout.getUserId().equals(userId)) {
            throw new BusinessException(AuthorizationError.ACCESS_DENIED);
        }
    }
}
