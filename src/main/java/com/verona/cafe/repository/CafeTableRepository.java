package com.verona.cafe.repository;

import com.verona.cafe.model.CafeTable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CafeTableRepository extends JpaRepository<CafeTable, Long> {
    Optional<CafeTable> findByTableNumber(String tableNumber);
}
