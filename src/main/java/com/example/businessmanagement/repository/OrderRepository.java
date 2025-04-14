package com.example.businessmanagement.repository;

import com.example.businessmanagement.dto.RevenueReportDto;
import com.example.businessmanagement.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByAddressEndId(Long addressId);

    List<Order> findByStatus(String status);

    List<Order> findByShipperUsernameAndStatus(String shipperUsername, String status);

    List<Order> findByShipper_IdAndStatus(Long shipperId, String status);

    List<Order> findByShopper_Id(Long shopperId);

    long countByShipperUsernameAndStatus(String shipperUsername, String status);

    List<Order> findByShopper_IdAndStatus(Long shopperId, String status);

    @Query("SELECT o FROM Order o JOIN FETCH o.addressEnd WHERE o.id = :id")
    Optional<Order> findWithAddressById(@Param("id") Long id);

    @Query("SELECT new com.example.businessmanagement.dto.RevenueReportDto(" +
            "YEAR(o.orderDate), " +
            "MONTH(o.orderDate), " +
            "SUM(o.totalAmount)) " +
            "FROM Order o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(o.orderDate), MONTH(o.orderDate)")
    List<RevenueReportDto> getMonthlyRevenue(@Param("startDate") Date startDate,
                                             @Param("endDate") Date endDate);
    @Query("SELECT od.product.id, SUM(od.quantity) " +
            "FROM OrderDetails od " +
            "JOIN od.order o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY od.product.id " +
            "ORDER BY SUM(od.quantity) DESC")
    List<Object[]> findTopSellingProducts(@Param("startDate") Date startDate,
                                          @Param("endDate") Date endDate,
                                          Pageable pageable);
}