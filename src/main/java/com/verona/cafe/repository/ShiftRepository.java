package com.verona.cafe.repository;

import com.verona.cafe.model.Shift;
import com.verona.cafe.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findByUser(User user);
}
