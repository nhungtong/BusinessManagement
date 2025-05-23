package com.example.businessmanagement.controller;

import com.example.businessmanagement.dto.OrderRequest;
import com.example.businessmanagement.dto.ReportDTO;
import com.example.businessmanagement.dto.ReviewForm;
import com.example.businessmanagement.entity.*;
import com.example.businessmanagement.repository.*;
import com.example.businessmanagement.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final ReportService reportService;
    private final OrderDetailService orderDetailService;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final VNPayService vnpayService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final UserService userService;

    public OrderController(OrderService orderService, ReportService reportService, OrderDetailService orderDetailService, UserRepository userRepository, CartService cartService, VNPayService vnpayService, OrderRepository orderRepository, PaymentRepository paymentRepository, UserService userService) {
        this.orderService = orderService;
        this.reportService = reportService;
        this.orderDetailService = orderDetailService;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.vnpayService = vnpayService;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.userService = userService;
    }

    // danh sách đơn hàng theo trạng thái
    @GetMapping("/list")
    public String listOrders(@RequestParam(value = "status", required = false) String status, Model model) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        List<String> orderStatuses = Arrays.asList("Đang chờ xác nhận", "Đang giao hàng", "Đã giao hàng","Đã hủy");

        model.addAttribute("orders", orders);
        model.addAttribute("orderStatuses", orderStatuses);
        model.addAttribute("selectedStatus", status);

        return "orders/list";
    }
    // danh sách đơn hàng chờ xác nhận
    @GetMapping("/pending")
    public String listPendingOrders(Model model) {
        List<Order> orders = orderService.getPendingOrders();
        model.addAttribute("orders", orders);
        return "orders/pending";
    }

    // cập nhật đơn hàng chờ xác nhận
    @PostMapping("/{orderId}/update-status")
    public String updateOrderStatus(@PathVariable Long orderId,
                                    @RequestParam String status,
                                    Principal principal) {
        orderService.updateOrderStatus(orderId, status, principal.getName());
        return "redirect:/orders/pending";
    }
    // Danh sách đơn hàng theo shipper (trạng thái Đang giao hàng)
    @GetMapping("/delivering")
    public String listShipperOrders(Model model, Principal principal) {
        List<Order> orders = orderService.getShipperOrders(principal.getName());
        model.addAttribute("orders", orders);
        return "orders/delivering";
    }

    // Cập nhật trạng thái đơn hàng
    @PostMapping("/update-delivering")
    public String updateShipperOrderStatus(@RequestParam Long orderId,
                                           @RequestParam String status,
                                           @RequestParam(required = false) String failureReason,
                                           Principal principal) {
        orderService.updateShipperOrderStatus(orderId, status, principal.getName(), failureReason);
        return "redirect:/orders/delivering";
    }

    // Hiển thị danh sách đơn hàng đã hoàn thành theo shipper
    @GetMapping("/completed")
    public String listCompletedOrders(Model model, Principal principal) {
        String shipperUsername = principal.getName();
        List<Order> completedOrders = orderService.getCompletedOrders(shipperUsername);
        double successRate = orderService.calculateSuccessRate(shipperUsername);

        model.addAttribute("completedOrders", completedOrders);
        model.addAttribute("successRate", successRate);

        return "orders/completed";
    }
    @PostMapping("/checkout")
    public String showCheckoutPage(
            @RequestParam(value = "selectedIds", required = false) List<Long> selectedIds,
            Model model,
            RedirectAttributes redirectAttributes,
            Principal principal) {

        if (selectedIds == null || selectedIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Vui lòng chọn ít nhất một sản phẩm để thanh toán.");
            return "redirect:/cart/view";
        }

        String username = principal.getName();

        List<Cart> selectedItems = cartService.getCartItemsByIds(selectedIds, username);
        int totalAmount = cartService.calculateTotalAmount(selectedItems);

        model.addAttribute("selectedItems", selectedItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("username", username);

        return "orders/checkout";
    }
    @PostMapping("/submit-checkout")
    public String submitCheckout(@RequestParam String province,
                                 @RequestParam String district,
                                 @RequestParam String ward,
                                 @RequestParam String street,
                                 @RequestParam String phoneNumber,
                                 @RequestParam String paymentMethod,
                                 @RequestParam(value = "selectedIds", required = false) List<Long> selectedIds,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes,
                                 Principal principal) {

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));


        List<Cart> selectedItems = cartService.getCartItemsByIds(selectedIds, username);

        int totalAmount = cartService.calculateTotalAmount(selectedItems);

        Order order = orderService.placeOrder(user, selectedItems, province, district, ward, street, phoneNumber, paymentMethod, totalAmount);

        if ("COD".equalsIgnoreCase(paymentMethod)) {
            redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công (COD)");
            return "redirect:/orders/success?orderId=" + order.getId();
        } else if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
            String orderInfo = "Thanh toán đơn hàng #" + order.getId();
            String vnpUrl = vnpayService.createPaymentUrl(orderInfo, String.valueOf(totalAmount), String.valueOf(order.getId()), request);
            return "redirect:" + vnpUrl;
        }
        redirectAttributes.addFlashAttribute("error", "Phương thức thanh toán không hợp lệ");
        return "redirect:/checkout";
    }

    // Giao diện đặt hàng thành công
    @GetMapping("/success")
    public String orderSuccess(@RequestParam("orderId") Long orderId,
                               @RequestParam(value = "transactionId", required = false) String transactionId,
                               Model model) {
        Optional<Order> optionalOrder = orderRepository.findWithAddressById(orderId);
        if (optionalOrder.isEmpty()) {
            model.addAttribute("message", "Không tìm thấy đơn hàng!");
            return "orders/failure";
        }

        Order order = optionalOrder.get();
        String deliveryAddress = "Không có địa chỉ giao hàng";
        if (order.getAddressEnd() != null) {
            deliveryAddress = String.join(", ",
                    order.getAddressEnd().getStreet(),
                    order.getAddressEnd().getWard(),
                    order.getAddressEnd().getDistrict(),
                    order.getAddressEnd().getProvince()
            );
        }
        model.addAttribute("deliveryAddress", deliveryAddress);

        model.addAttribute("order", order);

        if (transactionId != null && !transactionId.isEmpty()) {
            Optional<Payment> paymentOpt = paymentRepository.findByTransactionId(transactionId);
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                model.addAttribute("paymentStatus", payment.getStatus());
                model.addAttribute("transactionId", transactionId);
                model.addAttribute("paymentMethod", payment.getPaymentMethod());
            } else {
                model.addAttribute("paymentStatus", "Không xác định");
                model.addAttribute("paymentMethod", "VNPay (không rõ giao dịch)");
            }
        } else {
            model.addAttribute("paymentStatus", "Chờ thanh toán (COD)");
            model.addAttribute("paymentMethod", "COD");
        }

        return "orders/success";
    }


    // Hiển thị danh sách đơn hàng khi vào trang tra cứu
    @GetMapping("/track")
    public String trackOrders(Model model, Principal principal,@AuthenticationPrincipal UserDetails userDetails) {
        String username = principal.getName();
        List<Order> orders = orderService.getOrdersByShopper(username);

        model.addAttribute("orders", orders);
        model.addAttribute("statuses", List.of("Đang chờ xác nhận", "Đang giao hàng", "Đã giao hàng"));
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        Long userId = user.getId();

        List<Cart> cartItems = cartService.getCartItems(userId);

        int cartItemCount = cartItems.stream()
                .mapToInt(Cart::getQuantity)
                .sum();


        model.addAttribute("cartItemCount", cartItemCount);
        return "orders/track";
    }
    // Lọc đơn hàng theo trạng thái
    @GetMapping("/track/filter")
    public String filterOrders(@RequestParam("status") String status, Model model, Principal principal,@AuthenticationPrincipal UserDetails userDetails) {
        String username = principal.getName();
        List<Order> filteredOrders = orderService.getOrdersByStatus(username, status);

        if (filteredOrders == null || filteredOrders.isEmpty()) {
            model.addAttribute("message", "Không có đơn hàng nào trong trạng thái: " + status);
            filteredOrders = new ArrayList<>();
        }
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        Long userId = user.getId();

        List<Cart> cartItems = cartService.getCartItems(userId);

        int cartItemCount = cartItems.stream()
                .mapToInt(Cart::getQuantity)
                .sum();

        model.addAttribute("cartItemCount", cartItemCount);
        model.addAttribute("orders", filteredOrders);
        model.addAttribute("statuses", List.of("Đang chờ xác nhận", "Đang giao hàng", "Đã giao hàng"));

        return "orders/track";
    }
    @GetMapping("/detail/{id}")
    public String viewOrderDetail(@PathVariable("id") Long orderId, Model model,@AuthenticationPrincipal UserDetails userDetails) {
        Order order = orderService.getOrderById(orderId);
        List<OrderDetails> orderDetails = orderDetailService.getOrderDetailsByOrderId(orderId);
        if (order.getTotalAmount() == 0) {
            order.setTotalAmount(1);
        }
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        Long userId = user.getId();

        List<Cart> cartItems = cartService.getCartItems(userId);
        // Tính tổng số lượng sản phẩm trong giỏ hàng
        int cartItemCount = cartItems.stream()
                .mapToInt(Cart::getQuantity)
                .sum();
        model.addAttribute("cartItemCount", cartItemCount);
        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);

        return "orders/detail";
    }
    // Hiển thị trang đánh giá sản phẩm của đơn hàng
    @GetMapping("/review/{orderId}")
    public String reviewOrder(@PathVariable Long orderId, Model model, Principal principal) {
        List<Product> products = orderService.handleOrder(orderId);

        // Tạo danh sách rỗng các reviewDTO tương ứng với mỗi sản phẩm
        List<ReportDTO> reviewList = new ArrayList<>();
        for (Product p : products) {
            ReportDTO dto = new ReportDTO();
            dto.setProductId(p.getId());
            dto.setProductName(p.getProductName());
            reviewList.add(dto);
        }

        ReviewForm reviewForm = new ReviewForm();
        reviewForm.setReviews(reviewList);

        model.addAttribute("products", products);
        model.addAttribute("reviewForm", reviewForm);
        return "orders/review";
    }


    // Lưu đánh giá
    @PostMapping("/review/submit")
    public String submitReview(@ModelAttribute("reviewForm") ReviewForm reviewForm, Principal principal) {
        Long shopperId = orderService.getUserByUsername(principal.getName()).getId();

        for (ReportDTO review : reviewForm.getReviews()) {
            reportService.saveReview(shopperId, review.getProductId(), review.getRating(), review.getFeedback());
        }

        return "redirect:/orders/track";
    }
}
