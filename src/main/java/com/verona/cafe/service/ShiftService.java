package com.verona.cafe.service;

import com.verona.cafe.model.Shift;
import com.verona.cafe.model.User;
import com.verona.cafe.repository.ShiftRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShiftService {
    private final ShiftRepository shiftRepository;

    public ShiftService(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    public List<Shift> getShiftsByUser(User user) {
        return shiftRepository.findByUser(user);
    }

    public Shift getShiftById(Long id) {
        return shiftRepository.findById(id).orElse(null);
    }

    public Shift saveShift(Shift shift) {
        return shiftRepository.save(shift);
    }

    public void deleteShift(Long id) {
        shiftRepository.deleteById(id);
    }
}
