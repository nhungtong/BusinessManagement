package com.example.businessmanagement.repository;

import com.example.businessmanagement.entity.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetails, Long> {
    // Lấy danh sách sản phẩm của đơn hàng
    List<OrderDetails> findByOrderId(Long orderId);
}
