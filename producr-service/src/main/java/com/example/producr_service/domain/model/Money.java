package com.example.producr_service.domain.model;

import com.example.common.exception.BusinessException;
import com.example.producr_service.domain.error.DomainProductError;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

//lớp bọc để khơi tạo tiền chuẩn quy tắc
@Getter
public final class Money {

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount;
    }


    public static Money of(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(DomainProductError.MONEY_AMOUNT_NEGATIVE);
        }
        return new Money(amount);
    }


    public static Money of(long amount) {
        return of(BigDecimal.valueOf(amount));
    }



    // check xem amount của this có nhỏ hơn orther hay k
    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    // check xem amount của this có lớn  hơn orther hay k
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}
