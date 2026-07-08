package com.verona.cafe.service;

import com.verona.cafe.model.*;
import com.verona.cafe.repository.CafeTableRepository;
import com.verona.cafe.repository.CustomerRepository;
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
    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository,
                        CafeTableRepository tableRepository,
                        MenuItemRepository menuItemRepository,
                        CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.tableRepository = tableRepository;
        this.menuItemRepository = menuItemRepository;
        this.customerRepository = customerRepository;
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order ID: " + id));
    }

    public Optional<Order> getActiveOrderByTable(Long tableId) {
        return orderRepository.findByTableIdAndStatus(tableId, OrderStatus.ACTIVE);
    }

    public Order startOrderWithCustomer(Long tableId, String customerPhone, String customerName, User user) {
        CafeTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid table ID"));

        table.setStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);

        Customer customer = customerRepository.findByPhoneNumber(customerPhone)
                .orElseGet(() -> Customer.builder()
                        .phoneNumber(customerPhone)
                        .name(customerName)
                        .totalSpent(0.0)
                        .build());
        
        if (!customer.getName().equalsIgnoreCase(customerName)) {
            customer.setName(customerName);
        }
        customerRepository.save(customer);

        Order order = Order.builder()
                .table(table)
                .user(user)
                .customer(customer)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.ACTIVE)
                .orderItems(new ArrayList<>())
                .totalPrice(0.0)
                .build();

        return orderRepository.save(order);
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
        Order order = getActiveOrderByTable(tableId)
                .orElseThrow(() -> new IllegalStateException("Vui lòng nhập thông tin khách hàng trước!"));
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

        Customer customer = order.getCustomer();
        if (customer != null) {
            double totalWithVat = order.getTotalPrice() * 1.1;
            customer.setTotalSpent(customer.getTotalSpent() + totalWithVat);
            customerRepository.save(customer);
        }

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

    public java.util.List<java.util.Map<String, Object>> getRevenueByStaff(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        java.util.stream.Stream<Order> stream = getAllCompletedOrders().stream();
        if (startDate != null) {
            java.time.LocalDateTime start = startDate.atStartOfDay();
            java.time.LocalDateTime end = (endDate != null) ? endDate.atTime(java.time.LocalTime.MAX) : java.time.LocalDateTime.MAX;
            stream = stream.filter(o -> !o.getOrderDate().isBefore(start) && !o.getOrderDate().isAfter(end));
        }

        java.util.Map<User, java.util.DoubleSummaryStatistics> stats = stream.collect(
                java.util.stream.Collectors.groupingBy(Order::getUser,
                        java.util.stream.Collectors.summarizingDouble(Order::getTotalPrice)));

        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (java.util.Map.Entry<User, java.util.DoubleSummaryStatistics> e : stats.entrySet()) {
            User u = e.getKey();
            java.util.DoubleSummaryStatistics s = e.getValue();
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("userId", u.getId());
            m.put("fullName", u.getFullName());
            m.put("revenue", s.getSum());
            m.put("ordersCount", s.getCount());
            result.add(m);
        }

        result.sort((a, b) -> Double.compare((Double) b.get("revenue"), (Double) a.get("revenue")));
        return result;
    }
}
