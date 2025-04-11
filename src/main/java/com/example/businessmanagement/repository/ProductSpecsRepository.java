package com.example.businessmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.businessmanagement.entity.ProductSpecs;

import java.util.List;

@Repository
public interface ProductSpecsRepository extends JpaRepository<ProductSpecs, Integer> {
    List<ProductSpecs> findByProductIdOrderByDisplayOrderAsc(Long productId);
}
