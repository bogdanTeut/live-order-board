package com.cryptoinc.marketplace.validators;

import com.cryptoinc.marketplace.domain.Order;

import java.math.BigDecimal;
import java.util.Objects;

public class OrderValidatorImpl implements OrderValidator {

    @Override
    public void validate(Order order) {
        if (Objects.isNull(order.getUserId())) {
            throw new IllegalArgumentException("Bad user id.");
        }

        if (order.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Bad quantity.");
        }

        if (order.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Bad price.");
        }
    }
}
