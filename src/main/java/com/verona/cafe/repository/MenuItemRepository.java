package com.verona.cafe.repository;

import com.verona.cafe.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategoryId(Long categoryId);

    List<MenuItem> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    @Query("SELECT oi.menuItem FROM OrderItem oi WHERE oi.order.status = com.verona.cafe.model.OrderStatus.COMPLETED GROUP BY oi.menuItem ORDER BY SUM(oi.quantity) DESC")
    List<MenuItem> findTopSellingMenuItems(Pageable pageable);
}

