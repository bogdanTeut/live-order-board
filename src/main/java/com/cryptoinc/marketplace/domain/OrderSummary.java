package com.cryptoinc.marketplace.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class OrderSummary {
    private final BigDecimal quantity;
    private final BigDecimal price;

    public OrderSummary(
            final BigDecimal quantity,
            final BigDecimal price) {
        this.quantity = quantity;
        this.price = price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderSummary that = (OrderSummary) o;
        return Objects.equals(quantity, that.quantity) &&
                Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, price);
    }

    @Override
    public String toString() {
        return "OrderSummary{" +
                "quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
