package com.example.businessmanagement.repository;

import com.example.businessmanagement.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByPromoCode(String promoCode);
    List<Promotion> findByPromoCodeContainingIgnoreCase(String promoCode);
}
