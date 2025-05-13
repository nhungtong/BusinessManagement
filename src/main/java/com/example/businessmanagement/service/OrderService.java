package com.example.businessmanagement.service;

import com.example.businessmanagement.dto.RevenueReportDto;
import com.example.businessmanagement.dto.TopSellingProductDto;
import com.example.businessmanagement.entity.OrderDetails;
import com.example.businessmanagement.entity.*;
import com.example.businessmanagement.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailsRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CartService cartService;
    private final WarrantyRepository warrantyRepository;

    public OrderService(OrderRepository orderRepository, OrderDetailRepository orderDetailsRepository, UserRepository userRepository, AddressRepository addressRepository, CartService cartService, WarrantyRepository warrantyRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailsRepository = orderDetailsRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.cartService = cartService;
        this.warrantyRepository = warrantyRepository;
    }

    // danh sách đơn hàng
    public List<Order> getOrdersByStatus(String status) {
        if (status == null || status.isEmpty()) {
            return orderRepository.findAll();
        }
        return orderRepository.findByStatus(status);
    }

    // Lấy danh sách đơn hàng chờ xác nhận
    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus("Đang chờ xác nhận");
    }

    // Cập nhật trạng thái đơn hàng
    @Transactional
    public void updateOrderStatus(Long orderId, String status, String shipperUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        User shipper = userRepository.findByUsername(shipperUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy shipper"));

        if ("Đang giao hàng".equals(status)) {
            order.setStatus(status);
            order.setShipper(shipper);
            orderRepository.save(order);
        }
    }

    // danh sách đơn hàng theo id và trạng thái
    public List<Order> getShipperOrders(String shipperUsername) {
        User shipper = userRepository.findByUsername(shipperUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy shipper"));

        return orderRepository.findByShipper_IdAndStatus(shipper.getId(), "Đang giao hàng");
    }

    // cập nhật trạng thái
    @Transactional
    public void updateShipperOrderStatus(Long orderId, String status, String shipperUsername, String failureReason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        User shipper = userRepository.findByUsername(shipperUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy shipper"));

        order.setStatus(status);
        order.setShipper(shipper);

        if ("Đã giao hàng".equals(status)) {
            order.setDeliveryDate(new Date());
            order.setFailureReason(null);
        } else if ("Đã hủy".equals(status)) {
            order.setFailureReason(failureReason);
            order.setDeliveryDate(null);
        }

        orderRepository.save(order);
    }

    // Lấy danh sách đơn hàng đã hoàn thành của shipper
    public List<Order> getCompletedOrders(String shipperUsername) {
        return orderRepository.findByShipperUsernameAndStatus(shipperUsername, "Đã giao hàng");
    }

    // Tính tỷ lệ đơn hàng giao thành công
    public double calculateSuccessRate(String shipperUsername) {
        long totalDelivered = orderRepository.countByShipperUsernameAndStatus(shipperUsername, "Đã giao hàng");
        long totalFailed = orderRepository.countByShipperUsernameAndStatus(shipperUsername, "Đã hủy");

        long totalOrders = totalDelivered + totalFailed;
        return (totalOrders == 0) ? 0 : ((double) totalDelivered / totalOrders) * 100;
    }
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn hàng với ID = " + id));
    }

    // Lấy danh sách đơn hàng theo shopperUserId
    public List<Order> getOrdersByShopper(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByShopper_Id(user.getId());
    }

    // Lấy danh sách đơn hàng theo shopperUserId và trạng thái
    public List<Order> getOrdersByStatus(String username, String status) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByShopper_IdAndStatus(user.getId(), status);
    }
    public Order placeOrder(User user, List<Cart> cartItems, String province, String district, String ward,
                             String street, String phoneNumber,String paymentMethod, int totalAmount) {

        Address newAddress = new Address(province, district, ward, street);
        addressRepository.save(newAddress);

        Order order = new Order();
        order.setShopper(user);
        order.setPhoneNumber(phoneNumber);
        order.setAddressEnd(newAddress);
        order.setStatus("Đang chờ xác nhận");
        order.setOrderDate(new Date());
        order.setPayment_method(paymentMethod);
        order.setTotalAmount(totalAmount);

        orderRepository.save(order);

        Warranty warranty = new Warranty();
        warranty.setOrder(order);
        warranty.setStartDate(new Date());
        warranty.setEndDate(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365 * 2));
        warrantyRepository.save(warranty);

        for (Cart item : cartItems) {
            OrderDetails detail = new OrderDetails();
            detail.setOrder(order);
            detail.setProduct(item.getProduct());
            detail.setQuantity(item.getQuantity());

            orderDetailsRepository.save(detail);

            cartService.removeFromCart(item.getId());
        }

        return order;
    }
    public List<RevenueReportDto> getRevenueBetween(Date start, Date end) {
        return orderRepository.getMonthlyRevenue(start, end);
    }
    public List<TopSellingProductDto> getTopSellingProducts(Date startDate, Date endDate) {

        Pageable pageable = PageRequest.of(0, 5);
        List<Object[]> results = orderRepository.findTopSellingProducts(startDate, endDate, pageable);

        List<TopSellingProductDto> topSellingProducts = new ArrayList<>();
        for (Object[] result : results) {
            Long productId = (Long) result[0];
            Long totalQuantitySold = (Long) result[1];

            TopSellingProductDto dto = new TopSellingProductDto(productId, totalQuantitySold);
            topSellingProducts.add(dto);
        }

        return topSellingProducts;
    }
    public List<Product> handleOrder(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);

        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found");
        }

        Order order = orderOpt.get();

        List<OrderDetails> orderDetails = orderDetailsRepository.findByOrderId(order.getId());

        return orderDetails.stream()
                .map(OrderDetails::getProduct)
                .collect(Collectors.toList());
    }
}
