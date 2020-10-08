package com.cryptoinc.marketplace;

import com.cryptoinc.marketplace.domain.Order;
import com.cryptoinc.marketplace.domain.OrderSummary;
import com.cryptoinc.marketplace.domain.OrderType;

import java.util.List;

public interface LiveOrderBoard {
    void placeOrder(Order order);
    void cancelOrder(Order order);
    List<OrderSummary> getOrderSummary(OrderType orderType);
}
