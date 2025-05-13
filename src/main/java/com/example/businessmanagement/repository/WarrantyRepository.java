package com.example.businessmanagement.repository;

import com.example.businessmanagement.entity.Warranty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarrantyRepository extends JpaRepository<Warranty, Long> {
    Optional<Warranty> findByOrder_Id(Long orderId);
}
