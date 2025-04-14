package com.example.businessmanagement.controller;

import com.example.businessmanagement.config.VnPayConfig;
import com.example.businessmanagement.entity.Order;
import com.example.businessmanagement.entity.Payment;
import com.example.businessmanagement.repository.OrderRepository;
import com.example.businessmanagement.repository.PaymentRepository;
import com.example.businessmanagement.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/payment")
public class VNPayController {
    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private VnPayConfig vnPayConfig;

    // Hiển thị form để nhập mã đơn hàng
    @GetMapping("/form")
    public String showPaymentForm(Model model) {
        model.addAttribute("orderId", Long.valueOf(0));
        return "payments/payment-form";
    }

    // Xử lý thanh toán
    @PostMapping("/process")
    public String createPayment(@RequestParam("orderId") Long orderId, HttpServletRequest request) {

        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            return "redirect:/create?error=true";
        }

        Order order = optionalOrder.get();
        String orderInfo = "Thanh toan don hang #" + order.getId();
        String amount = String.valueOf(order.getTotalAmount());

        String paymentUrl = vnPayService.createPaymentUrl(orderInfo, amount, String.valueOf(orderId), request);

        return "redirect:" + paymentUrl;
    }

    @GetMapping("/return")
    public String handlePaymentResult(@RequestParam Map<String, String> params, Model model) {

        String paymentMethod = params.get("vnp_PaymentMethod");
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            paymentMethod = "BankTransfer";
        }

        Long orderId = null;
        try {
            orderId = Long.valueOf(params.get("vnp_TxnRef"));
        } catch (NumberFormatException | NullPointerException e) {
            model.addAttribute("status", "Failed");
            model.addAttribute("transactionId", "N/A");
            return "payments/payment-result";
        }

        String transactionId = params.getOrDefault("vnp_TransactionNo", "N/A");

        String status = "Failed";
        if ("00".equals(params.get("vnp_ResponseCode"))) {
            status = "Completed";
        }

        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            model.addAttribute("status", "Failed");
            model.addAttribute("transactionId", transactionId);
            return "payments/payment-result";
        }

        Order order = optionalOrder.get();

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentMethod);
        payment.setTransactionId(transactionId);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(status);
        payment.setPaymentDate(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

        try {
            paymentRepository.save(payment);
        } catch (Exception e) {
            model.addAttribute("status", "Failed");
            model.addAttribute("transactionId", transactionId);
            return "payments/payment-result";
        }

        model.addAttribute("status", status);
        model.addAttribute("transactionId", transactionId);

        return "payments/payment-result";
    }
}
