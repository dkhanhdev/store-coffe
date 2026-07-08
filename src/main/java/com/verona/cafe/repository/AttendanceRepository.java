package com.verona.cafe.repository;

import com.verona.cafe.model.Attendance;
import com.verona.cafe.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserAndDate(User user, LocalDate date);
    List<Attendance> findByDate(LocalDate date);
    List<Attendance> findByUser(User user);
}
