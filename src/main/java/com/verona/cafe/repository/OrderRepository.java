package com.verona.cafe.repository;

import com.verona.cafe.model.Order;
import com.verona.cafe.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByStatusAndOrderDateBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);
    Optional<Order> findByTableIdAndStatus(Long tableId, OrderStatus status);
}
