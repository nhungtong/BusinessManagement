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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailsRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final StoreProductRepository storeProductRepository;

    public OrderService(OrderRepository orderRepository, OrderDetailRepository orderDetailsRepository, UserRepository userRepository, AddressRepository addressRepository, CartRepository cartRepository, CartService cartService, StoreProductRepository storeProductRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailsRepository = orderDetailsRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
        this.storeProductRepository = storeProductRepository;
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

        // Lấy thông tin shipper từ username
        User shipper = userRepository.findByUsername(shipperUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy shipper"));

        // Nếu chọn "Đang giao hàng", cập nhật trạng thái và shipper_id
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
        } else if ("Giao hàng không thành công".equals(status)) {
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
        long totalFailed = orderRepository.countByShipperUsernameAndStatus(shipperUsername, "Giao hàng không thành công");

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
    public Address getAddressById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ với ID: " + addressId));
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
        order.setShopper(user); // set đối tượng User trực tiếp
        order.setPhoneNumber(phoneNumber); // đúng tên biến là phoneNumber (viết hoa N)
        order.setAddressEnd(newAddress); // set trực tiếp entity Address
        order.setStatus("Đang chờ xác nhận");
        order.setOrderDate(new Date());
        order.setPayment_method(paymentMethod); // đúng tên field trong entity
        order.setTotalAmount(totalAmount); // kiểu int, không phải BigDecimal như trước

        orderRepository.save(order);


        for (Cart item : cartItems) {
            OrderDetails detail = new OrderDetails();
            detail.setOrder(order); // set entity Order
            detail.setProduct(item.getProduct()); // set entity Product
            detail.setQuantity(item.getQuantity());

            orderDetailsRepository.save(detail);

            cartService.removeFromCart(item.getId()); // xóa giỏ hàng
        }


        return order;
    }
    public List<RevenueReportDto> getRevenueBetween(Date start, Date end) {
        return orderRepository.getMonthlyRevenue(start, end);
    }
    public List<TopSellingProductDto> getTopSellingProducts(Date startDate, Date endDate) {
        // Truy vấn top 5 sản phẩm bán chạy
        Pageable pageable = PageRequest.of(0, 5);  // Lấy 5 sản phẩm
        List<Object[]> results = orderRepository.findTopSellingProducts(startDate, endDate, pageable);

        List<TopSellingProductDto> topSellingProducts = new ArrayList<>();
        for (Object[] result : results) {
            Long productId = (Long) result[0];
            Long totalQuantitySold = (Long) result[1];

            // Bạn có thể lấy thông tin chi tiết sản phẩm từ ProductRepository nếu cần
            TopSellingProductDto dto = new TopSellingProductDto(productId, totalQuantitySold);
            topSellingProducts.add(dto);
        }

        return topSellingProducts;
    }
}
