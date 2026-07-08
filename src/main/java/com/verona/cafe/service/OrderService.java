package com.verona.cafe.service;

import com.verona.cafe.model.*;
import com.verona.cafe.repository.CafeTableRepository;
import com.verona.cafe.repository.MenuItemRepository;
import com.verona.cafe.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CafeTableRepository tableRepository;
    private final MenuItemRepository menuItemRepository;

    public OrderService(OrderRepository orderRepository,
                        CafeTableRepository tableRepository,
                        MenuItemRepository menuItemRepository) {
        this.orderRepository = orderRepository;
        this.tableRepository = tableRepository;
        this.menuItemRepository = menuItemRepository;
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order ID: " + id));
    }

    public Optional<Order> getActiveOrderByTable(Long tableId) {
        return orderRepository.findByTableIdAndStatus(tableId, OrderStatus.ACTIVE);
    }

    public Order getOrCreateActiveOrder(Long tableId, User user) {
        Optional<Order> activeOrder = getActiveOrderByTable(tableId);
        if (activeOrder.isPresent()) {
            return activeOrder.get();
        }

        CafeTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid table ID"));

        table.setStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);

        Order order = Order.builder()
                .table(table)
                .user(user)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.ACTIVE)
                .orderItems(new ArrayList<>())
                .totalPrice(0.0)
                .build();

        return orderRepository.save(order);
    }

    public Order addItemToOrder(Long tableId, Long menuItemId, int quantity, User user) {
        Order order = getOrCreateActiveOrder(tableId, user);
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid menu item ID"));

        Optional<OrderItem> existingItemOpt = order.getOrderItems().stream()
                .filter(item -> item.getMenuItem().getId().equals(menuItemId))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            OrderItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            OrderItem newItem = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .quantity(quantity)
                    .price(menuItem.getPrice())
                    .build();
            order.getOrderItems().add(newItem);
        }

        recalculateTotal(order);
        return orderRepository.save(order);
    }

    public Order updateItemQuantity(Long tableId, Long menuItemId, int quantity) {
        Order order = getActiveOrderByTable(tableId)
                .orElseThrow(() -> new IllegalStateException("No active order found for table ID: " + tableId));

        if (quantity <= 0) {
            order.getOrderItems().removeIf(item -> item.getMenuItem().getId().equals(menuItemId));
        } else {
            order.getOrderItems().stream()
                    .filter(item -> item.getMenuItem().getId().equals(menuItemId))
                    .findFirst()
                    .ifPresent(item -> item.setQuantity(quantity));
        }

        recalculateTotal(order);
        return orderRepository.save(order);
    }

    public Order removeItemFromOrder(Long tableId, Long menuItemId) {
        return updateItemQuantity(tableId, menuItemId, 0);
    }

    public Order completeOrder(Long tableId, Double amountPaid, String paymentMethod) {
        Order order = getActiveOrderByTable(tableId)
                .orElseThrow(() -> new IllegalStateException("No active order found for table ID: " + tableId));

        order.setStatus(OrderStatus.COMPLETED);
        order.setAmountPaid(amountPaid);
        order.setPaymentMethod(paymentMethod);

        CafeTable table = order.getTable();
        table.setStatus(TableStatus.AVAILABLE);
        tableRepository.save(table);

        return orderRepository.save(order);
    }

    public void cancelOrder(Long tableId) {
        Optional<Order> orderOpt = getActiveOrderByTable(tableId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            CafeTable table = order.getTable();
            table.setStatus(TableStatus.AVAILABLE);
            tableRepository.save(table);
        }
    }

    private void recalculateTotal(Order order) {
        double total = order.getOrderItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        order.setTotalPrice(total);
    }

    public List<Order> getAllCompletedOrders() {
        return orderRepository.findByStatus(OrderStatus.COMPLETED);
    }

    public List<Order> getCompletedOrdersByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return orderRepository.findByStatusAndOrderDateBetween(OrderStatus.COMPLETED, start, end);
    }

    public Double getTotalRevenue() {
        return getAllCompletedOrders().stream()
                .mapToDouble(Order::getTotalPrice)
                .sum();
    }

    public Double getTotalCost() {
        return getAllCompletedOrders().stream()
                .flatMap(order -> order.getOrderItems().stream())
                .mapToDouble(item -> (item.getMenuItem().getCostPrice() != null ? item.getMenuItem().getCostPrice() : 0.0) * item.getQuantity())
                .sum();
    }

    public Double getTotalProfit() {
        return getTotalRevenue() - getTotalCost();
    }

    public Double getDailyRevenue() {
        LocalDate today = LocalDate.now();
        return getAllCompletedOrders().stream()
                .filter(o -> o.getOrderDate().toLocalDate().equals(today))
                .mapToDouble(Order::getTotalPrice)
                .sum();
    }

    public Double getDailyCost() {
        LocalDate today = LocalDate.now();
        return getAllCompletedOrders().stream()
                .filter(o -> o.getOrderDate().toLocalDate().equals(today))
                .flatMap(order -> order.getOrderItems().stream())
                .mapToDouble(item -> (item.getMenuItem().getCostPrice() != null ? item.getMenuItem().getCostPrice() : 0.0) * item.getQuantity())
                .sum();
    }

    public Double getDailyProfit() {
        return getDailyRevenue() - getDailyCost();
    }
}
