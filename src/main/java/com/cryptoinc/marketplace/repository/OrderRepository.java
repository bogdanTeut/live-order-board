package com.cryptoinc.marketplace.repository;

import com.cryptoinc.marketplace.domain.Order;

import java.util.List;

public interface OrderRepository {
    void saveOrder(Order order);
    void remove(Order order);
    List<Order> getOrders();
}
