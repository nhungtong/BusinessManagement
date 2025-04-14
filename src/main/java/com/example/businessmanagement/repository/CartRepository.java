package com.example.businessmanagement.repository;

import com.example.businessmanagement.entity.Cart;
import com.example.businessmanagement.entity.Product;
import com.example.businessmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByShopper(User shopper);
    Optional<Cart> findByShopperAndProduct(User shopper, Product product);
    List<Cart> findByIdInAndShopper_Username(List<Long> ids, String username);
}
