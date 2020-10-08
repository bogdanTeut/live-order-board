package com.cryptoinc.marketplace.validators;

import com.cryptoinc.marketplace.domain.Order;

public interface OrderValidator {
    void validate(Order order);
}
