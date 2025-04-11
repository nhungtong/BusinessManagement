package com.example.businessmanagement.controller;

import com.example.businessmanagement.entity.Payment;
import com.example.businessmanagement.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Hiển thị danh sách thanh toán có tìm kiếm theo tình trạng
    @GetMapping("/list")
    public String listPayments(@RequestParam(value = "status", required = false) String status, Model model) {
        List<Payment> payments = (status == null || status.isEmpty())
                ? paymentService.getAllPayments()
                : paymentService.getPaymentsByStatus(status);
        model.addAttribute("payments", payments);
        model.addAttribute("selectedStatus", status); // Giữ giá trị combobox khi tìm kiếm
        return "payments/list";
    }

    // Xóa thanh toán
    @PostMapping("/delete/{id}")
    public String deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return "redirect:/payments/list";
    }

}
