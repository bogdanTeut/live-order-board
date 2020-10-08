package com.cryptoinc.marketplace.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class Order {
    private final UUID userId;
    private final OrderType orderType;
    private final CoinType coinType;
    private final BigDecimal quantity;
    private final BigDecimal price;

    public Order(
            final UUID userId,
            final OrderType orderType,
            final CoinType coinType,
            final BigDecimal quantity,
            final BigDecimal price) {
        this.userId = userId;
        this.orderType = orderType;
        this.coinType = coinType;
        this.quantity = quantity;
        this.price = price;
    }

    public UUID getUserId() {
        return userId;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public CoinType getCoinType() {
        return coinType;
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
        Order order = (Order) o;
        return Objects.equals(userId, order.userId) &&
                orderType == order.orderType &&
                coinType == order.coinType &&
                Objects.equals(quantity, order.quantity) &&
                Objects.equals(price, order.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, orderType, coinType, quantity, price);
    }
}
