package com.cryptoinc.marketplace;

import com.cryptoinc.marketplace.domain.Order;
import com.cryptoinc.marketplace.domain.OrderSummary;
import com.cryptoinc.marketplace.repository.OrderRepository;
import com.cryptoinc.marketplace.validators.OrderValidator;
import org.hamcrest.MatcherAssert;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.cryptoinc.marketplace.domain.CoinType.Ethereum;
import static com.cryptoinc.marketplace.domain.OrderType.BUY;
import static com.cryptoinc.marketplace.domain.OrderType.SELL;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LiveOrderBoardTest {

    @Mock
    private OrderRepository orderRepository;

    private LiveOrderBoard liveOrderBoard;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        final OrderValidator orderValidator = new OrderValidatorImpl();
        this.liveOrderBoard = new LiveOrderBoardImpl(this.orderRepository, orderValidator);
    }

    @Test
    public void testPlaceOrder() {
        //given
        final Order order = new Order(randomUUID(), BUY, Ethereum, BigDecimal.valueOf(350.1), BigDecimal.valueOf(13.6));

        //when
        this.liveOrderBoard.placeOrder(order);

        //then
        verify(this.orderRepository).saveOrder(order);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Order is null.")
    public void givenNullOrder_whenPlacingOrder_thenItShouldThrowException() {
        this.liveOrderBoard.placeOrder(null);
    }

//    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Bad user id.")
//    public void givenNullUserId_whenPlacingOrder_thenItShouldThrowException() {
//        //given
//        final Order order = new Order(null, BUY, Ethereum, BigDecimal.valueOf(350.1), BigDecimal.valueOf(13.6));
//
//        //when
//        this.liveOrderBoard.placeOrder(order);
//    }
//
//    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Bad quantity.")
//    public void givenZeroQuantity_whenPlacingOrder_thenItShouldThrowException() {
//        //given
//        final Order order = new Order(randomUUID(), BUY, Ethereum, BigDecimal.valueOf(0), BigDecimal.valueOf(13.6));
//
//        //when
//        this.liveOrderBoard.placeOrder(order);
//    }
//
//    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Bad quantity.")
//    public void givenLessThanZeroQuantity_whenPlacingOrder_thenItShouldThrowException() {
//        //given
//        final Order order = new Order(randomUUID(), BUY, Ethereum, BigDecimal.valueOf(-350.1), BigDecimal.valueOf(13.6));
//
//        //when
//        this.liveOrderBoard.placeOrder(order);
//    }
//
//    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Bad price.")
//    public void givenZeroPrice_whenPlacingOrder_thenItShouldThrowException() {
//        //given
//        final Order order = new Order(randomUUID(), BUY, Ethereum, BigDecimal.valueOf(350.1), BigDecimal.valueOf(0));
//
//        //when
//        this.liveOrderBoard.placeOrder(order);
//    }
//
//    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Bad price.")
//    public void givenLessThanZeroPrice_whenPlacingOrder_thenItShouldThrowException() {
//        //given
//        final Order order = new Order(randomUUID(), BUY, Ethereum, BigDecimal.valueOf(350.1), BigDecimal.valueOf(-13.6));
//
//        //when
//        this.liveOrderBoard.placeOrder(order);
//    }

    @DataProvider(name = "negativeValidationTestCases")
    public Object[][] negativeValidationTestCases() {
        return new Object[][]{
                //user id       //quantity      //price       //expected message
                {null,          350.1,          13.6,         "Bad user id."},
                {randomUUID(),  0,              13.6,         "Bad quantity."},
                {randomUUID(),  -350.1,         13.6,         "Bad quantity."},
                {randomUUID(),  350.1,          0,            "Bad price."},
                {randomUUID(),  350.1,          -13.6,        "Bad price."}
        };
    }

    @Test(dataProvider = "negativeValidationTestCases")
    public void givenTheNegativeTestCase_whenPlacingOrder_thenItShouldThrowException(
            UUID userId,
            double quantity,
            double price,
            String expectedErrorMessage
    ) {
        //given
        Order order = new Order(userId, SELL, Ethereum, BigDecimal.valueOf(quantity), BigDecimal.valueOf(price));

        //when
        try {
            this.liveOrderBoard.placeOrder(order);
        } catch (IllegalArgumentException e) {
            //then
            assertThat(e.getMessage(), containsString(expectedErrorMessage));
        }
    }

    @Test
    public void testCancelOrder() {
        //given
        final Order order = new Order(randomUUID(), BUY, Ethereum, BigDecimal.valueOf(350.1), BigDecimal.valueOf(13.6));

        //when
        this.liveOrderBoard.cancelOrder(order);

        //then
        verify(this.orderRepository).remove(order);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Order is null.")
    public void givenNullOrder_whenCancelingOrder_thenItShouldThrowException() {
        this.liveOrderBoard.cancelOrder(null);
    }

    @Test
    public void testGetOrderSummary() {
        //given
        when(this.orderRepository.getOrders())
                .thenReturn(List.of(
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(350.1), BigDecimal.valueOf(13.6)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(50.5), BigDecimal.valueOf(14)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(441.8), BigDecimal.valueOf(13.9))
                ));

        List<OrderSummary> expectedOrderSummaryList = List.of(
                new OrderSummary(BigDecimal.valueOf(350.1), BigDecimal.valueOf(13.6)),
                new OrderSummary(BigDecimal.valueOf(50.5), BigDecimal.valueOf(14)),
                new OrderSummary(BigDecimal.valueOf(441.8), BigDecimal.valueOf(13.9))
        );

        //when
        List<OrderSummary> orderSummaryList = this.liveOrderBoard.getOrderSummary(SELL);

        //then
        MatcherAssert.assertThat(orderSummaryList, containsInAnyOrder(expectedOrderSummaryList.toArray()));
    }

    @Test
    public void givenNoOrders_whenGettingOrderSummary_thenItShouldReturnAnEmptyList() {
        //given
        when(this.orderRepository.getOrders()).thenReturn(Collections.emptyList());

        //when
        List<OrderSummary> orderSummaryList = this.liveOrderBoard.getOrderSummary(SELL);

        //then
        MatcherAssert.assertThat(orderSummaryList, empty());
    }

    @Test
    public void givenMultipleOrdersWithTheSamePrice_whenGettingOrderSummary_thenQuantitiesShouldBeMerged() {
        //given
        when(this.orderRepository.getOrders())
                .thenReturn(List.of(
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(350.1), BigDecimal.valueOf(13.6)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(50.5), BigDecimal.valueOf(14)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(441.8), BigDecimal.valueOf(13.9)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(3.5), BigDecimal.valueOf(13.6))
                ));

        List<OrderSummary> expectedOrderSummaryList = List.of(
                new OrderSummary(BigDecimal.valueOf(353.6), BigDecimal.valueOf(13.6)),
                new OrderSummary(BigDecimal.valueOf(50.5), BigDecimal.valueOf(14)),
                new OrderSummary(BigDecimal.valueOf(441.8), BigDecimal.valueOf(13.9))
        );

        //when
        List<OrderSummary> orderSummaryList = this.liveOrderBoard.getOrderSummary(SELL);

        //then
        MatcherAssert.assertThat(orderSummaryList, containsInAnyOrder(expectedOrderSummaryList.toArray()));
    }

    @Test
    public void testGetOrderSummaryShouldOnlyFetchForTheGivenOrderType() {
        //given
        when(this.orderRepository.getOrders())
                .thenReturn(List.of(
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(350.1), BigDecimal.valueOf(13.6)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(50.5), BigDecimal.valueOf(14)),
                        new Order(UUID.randomUUID(), BUY, Ethereum, BigDecimal.valueOf(441.8), BigDecimal.valueOf(13.9))
                ));

        List<OrderSummary> expectedOrderSummaryList = List.of(
                new OrderSummary(BigDecimal.valueOf(350.1), BigDecimal.valueOf(13.6)),
                new OrderSummary(BigDecimal.valueOf(50.5), BigDecimal.valueOf(14))
        );

        //when
        List<OrderSummary> orderSummaryList = this.liveOrderBoard.getOrderSummary(SELL);

        //then
        MatcherAssert.assertThat(orderSummaryList, containsInAnyOrder(expectedOrderSummaryList.toArray()));
    }

    @Test
    public void givenMultipleSELLOrders_whenGettingOrderSummary_thenItShouldDisplayLowestPricesFirst() {
        //given
        when(this.orderRepository.getOrders())
                .thenReturn(List.of(
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(441.8), BigDecimal.valueOf(13.9)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(50.5), BigDecimal.valueOf(14)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(350.1), BigDecimal.valueOf(13.6))
                ));

        List<OrderSummary> expectedOrderSummaryList = List.of(
                new OrderSummary(BigDecimal.valueOf(350.1), BigDecimal.valueOf(13.6)),
                new OrderSummary(BigDecimal.valueOf(441.8), BigDecimal.valueOf(13.9)),
                new OrderSummary(BigDecimal.valueOf(50.5), BigDecimal.valueOf(14))
        );

        //when
        List<OrderSummary> orderSummaryList = this.liveOrderBoard.getOrderSummary(SELL);

        //then
        MatcherAssert.assertThat(orderSummaryList, contains(expectedOrderSummaryList.toArray()));
    }

    @Test
    public void givenMultipleBUYOrders_whenGettingOrderSummary_thenItShouldDisplayHighestPricesFirst() {
        //given
        when(this.orderRepository.getOrders())
                .thenReturn(List.of(
                        new Order(UUID.randomUUID(), BUY, Ethereum, BigDecimal.valueOf(441.8), BigDecimal.valueOf(13.9)),
                        new Order(UUID.randomUUID(), BUY, Ethereum, BigDecimal.valueOf(50.5), BigDecimal.valueOf(14)),
                        new Order(UUID.randomUUID(), BUY, Ethereum, BigDecimal.valueOf(350.1), BigDecimal.valueOf(13.6))
                ));

        List<OrderSummary> expectedOrderSummaryList = List.of(
                new OrderSummary(BigDecimal.valueOf(50.5), BigDecimal.valueOf(14)),
                new OrderSummary(BigDecimal.valueOf(441.8), BigDecimal.valueOf(13.9)),
                new OrderSummary(BigDecimal.valueOf(350.1), BigDecimal.valueOf(13.6))
        );

        //when
        List<OrderSummary> orderSummaryList = this.liveOrderBoard.getOrderSummary(BUY);

        //then
        MatcherAssert.assertThat(orderSummaryList, contains(expectedOrderSummaryList.toArray()));
    }

    @Test
    public void givenNoOrdersForOrderType_whenGettingOrderSummary_thenItShouldReturnAnEmptyList() {
        //given
        when(this.orderRepository.getOrders())
                .thenReturn(List.of(
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(350.1), BigDecimal.valueOf(13.6)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(50.5), BigDecimal.valueOf(14)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(441.8), BigDecimal.valueOf(13.9)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(3.5), BigDecimal.valueOf(13.6))
                ));

        //when
        List<OrderSummary> orderSummaryList = this.liveOrderBoard.getOrderSummary(BUY);

        //then
        MatcherAssert.assertThat(orderSummaryList, empty());
    }

    @Test
    public void givenMultipleTypesOrders_testGetOrderSummary() {
        //given
        when(this.orderRepository.getOrders())
                .thenReturn(List.of(
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(350.1), BigDecimal.valueOf(13.6)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(50.5), BigDecimal.valueOf(14)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(441.8), BigDecimal.valueOf(13.9)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(3.5), BigDecimal.valueOf(13.6)),
                        new Order(UUID.randomUUID(), BUY, Ethereum, BigDecimal.valueOf(45.5), BigDecimal.valueOf(13.5)),
                        new Order(UUID.randomUUID(), BUY, Ethereum, BigDecimal.valueOf(232.5), BigDecimal.valueOf(12.9)),
                        new Order(UUID.randomUUID(), BUY, Ethereum, BigDecimal.valueOf(10.2), BigDecimal.valueOf(13.5))
                ));

        final List<OrderSummary> expectedSellOrderSummaryList = List.of(
                new OrderSummary(BigDecimal.valueOf(353.6), BigDecimal.valueOf(13.6)),
                new OrderSummary(BigDecimal.valueOf(441.8), BigDecimal.valueOf(13.9)),
                new OrderSummary(BigDecimal.valueOf(50.5), BigDecimal.valueOf(14))
        );

        final List<OrderSummary> expectedBuyOrdersSummaryList = List.of(
                new OrderSummary(BigDecimal.valueOf(55.7), BigDecimal.valueOf(13.5)),
                new OrderSummary(BigDecimal.valueOf(232.5), BigDecimal.valueOf(12.9))
        );

        //when
        final List<OrderSummary> sellSummaryList = this.liveOrderBoard.getOrderSummary(SELL);
        final List<OrderSummary> buySummaryList = this.liveOrderBoard.getOrderSummary(BUY);

        //then
        assertThat(sellSummaryList, contains(expectedSellOrderSummaryList.toArray()));
        assertThat(buySummaryList, contains(expectedBuyOrdersSummaryList.toArray()));
    }

    @Test
    public void givenMoreThanTenOrders_whenGettingOrderSummary_thenItShouldReturnTopTen() {
        //given
        when(this.orderRepository.getOrders())
                .thenReturn(List.of(
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(350.1), BigDecimal.valueOf(13.6)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(50.5), BigDecimal.valueOf(14)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(441.8), BigDecimal.valueOf(13.9)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(3.5), BigDecimal.valueOf(13.6)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(45.5), BigDecimal.valueOf(13.5)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(232.5), BigDecimal.valueOf(12.9)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(10.2), BigDecimal.valueOf(13.5)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(10.0), BigDecimal.valueOf(14.1)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(10.0), BigDecimal.valueOf(14.2)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(10.0), BigDecimal.valueOf(14.3)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(10.0), BigDecimal.valueOf(14.4)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(10.0), BigDecimal.valueOf(14.5)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(10.0), BigDecimal.valueOf(14.6)),
                        new Order(UUID.randomUUID(), SELL, Ethereum, BigDecimal.valueOf(10.0), BigDecimal.valueOf(14.7))
                ));

        List<OrderSummary> expectedOrderSummaryList = List.of(
                new OrderSummary(BigDecimal.valueOf(232.5), BigDecimal.valueOf(12.9)),
                new OrderSummary(BigDecimal.valueOf(55.7), BigDecimal.valueOf(13.5)),
                new OrderSummary(BigDecimal.valueOf(353.6), BigDecimal.valueOf(13.6)),
                new OrderSummary(BigDecimal.valueOf(441.8), BigDecimal.valueOf(13.9)),
                new OrderSummary(BigDecimal.valueOf(50.5), BigDecimal.valueOf(14)),
                new OrderSummary(BigDecimal.valueOf(10.0), BigDecimal.valueOf(14.1)),
                new OrderSummary(BigDecimal.valueOf(10.0), BigDecimal.valueOf(14.2)),
                new OrderSummary(BigDecimal.valueOf(10.0), BigDecimal.valueOf(14.3)),
                new OrderSummary(BigDecimal.valueOf(10.0), BigDecimal.valueOf(14.4)),
                new OrderSummary(BigDecimal.valueOf(10.0), BigDecimal.valueOf(14.5))
        );

        //when
        List<OrderSummary> orderSummaryList = this.liveOrderBoard.getOrderSummary(SELL);

        //then
        MatcherAssert.assertThat(orderSummaryList, contains(expectedOrderSummaryList.toArray()));
    }
}
