package com.verona.cafe.service;

import com.verona.cafe.model.Attendance;
import com.verona.cafe.model.User;
import com.verona.cafe.repository.AttendanceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public Attendance getByUserAndDate(User user, LocalDate date) {
        return attendanceRepository.findByUserAndDate(user, date).orElse(null);
    }

    public List<Attendance> getByDate(LocalDate date) {
        return attendanceRepository.findByDate(date);
    }

    public List<Attendance> getByUser(User user) {
        return attendanceRepository.findByUser(user);
    }

    public Attendance save(Attendance a) {
        return attendanceRepository.save(a);
    }

    public void delete(Long id) {
        attendanceRepository.deleteById(id);
    }
}
