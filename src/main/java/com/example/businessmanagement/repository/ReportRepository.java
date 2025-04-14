package com.example.businessmanagement.repository;

import com.example.businessmanagement.entity.Product;
import com.example.businessmanagement.entity.Report;
import com.example.businessmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByShopperAndProduct(User shopper, Product product);
    List<Report> findByProductId(Long productId);
}
