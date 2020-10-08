package com.cryptoinc.marketplace;

import com.cryptoinc.marketplace.domain.Order;
import com.cryptoinc.marketplace.domain.OrderSummary;
import com.cryptoinc.marketplace.domain.OrderType;
import com.cryptoinc.marketplace.repository.OrderRepository;
import com.cryptoinc.marketplace.validators.OrderValidator;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.cryptoinc.marketplace.domain.OrderType.SELL;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.*;

public class LiveOrderBoardImpl implements LiveOrderBoard {

    private static final int MAX_ORDERS_TO_DISPLAY = 10;
    private final OrderRepository orderRepository;
    private final OrderValidator orderValidator;

    public LiveOrderBoardImpl(final OrderRepository orderRepository, final OrderValidator orderValidator) {
        this.orderRepository = orderRepository;
        this.orderValidator = orderValidator;
    }

    @Override
    public void placeOrder(final Order order) {
        Objects.requireNonNull(order, "Order is null.");

        this.orderValidator.validate(order);
        this.orderRepository.saveOrder(order);
    }

    @Override
    public void cancelOrder(final Order order) {
        Objects.requireNonNull(order, "Order is null.");

        this.orderRepository.remove(order);
    }

    @Override
    public List<OrderSummary> getOrderSummary(final OrderType orderType) {
        final List<Order> orderList = this.orderRepository.getOrders();

        final Map<OrderType, Map<BigDecimal, OrderSummary>> ordersGroupedByTypeByPrice = orderList.stream()
                //group by order type
                .collect(groupingBy(Order::getOrderType,
                                    //group by price
                                    groupingBy(Order::getPrice,
                                               //map to OrderSummary
                                               mapping(orderOrderSummaryMapper(),
                                                       //reduce by summing quantities
                                                       reducing(identityOrderSummary(), reduceOrderSummary())
                                               )
                                    )
                        )
                );

        if (!ordersGroupedByTypeByPrice.containsKey(orderType)) {
            return Collections.emptyList();
        }

        final Comparator<OrderSummary> orderTypeComparator = getComparator(orderType);

        return ordersGroupedByTypeByPrice.get(orderType)
                .values()
                .stream()
                .sorted(orderTypeComparator)
                .limit(MAX_ORDERS_TO_DISPLAY)
                .collect(Collectors.toList());
    }

    private Comparator<OrderSummary> getComparator(final OrderType orderType) {
        return orderType == SELL ? Comparator.comparing(OrderSummary::getPrice) : Comparator.comparing(OrderSummary::getPrice).reversed();
    }

    private Function<Order, OrderSummary> orderOrderSummaryMapper() {
        return order -> new OrderSummary(order.getQuantity(), order.getPrice());
    }

    private OrderSummary identityOrderSummary() {
        return new OrderSummary(ZERO, ZERO);
    }

    private BinaryOperator<OrderSummary> reduceOrderSummary() {
        return (orderSummary1, orderSummary2) -> new OrderSummary(
                orderSummary1.getQuantity().add(orderSummary2.getQuantity()),
                orderSummary2.getPrice()
        );
    }
}
