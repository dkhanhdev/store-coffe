package com.verona.cafe.repository;

import com.verona.cafe.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPhoneNumber(String phoneNumber);
    List<Customer> findAllByOrderByTotalSpentDesc();
    List<Customer> findByNameContainingIgnoreCaseOrPhoneNumberContaining(String name, String phoneNumber);
}
