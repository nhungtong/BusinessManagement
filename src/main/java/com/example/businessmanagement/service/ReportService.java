package com.example.businessmanagement.service;

import com.example.businessmanagement.entity.*;
import com.example.businessmanagement.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ReportService(ReportRepository reportRepository, OrderDetailRepository orderDetailRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.reportRepository = reportRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    // Lưu đánh giá sản phẩm
    public void saveReview(Long shopperId, Long productId, int rating, String feedback) {
        User shopper = userRepository.findById(shopperId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        Optional<Report> existingReport = reportRepository.findByShopperAndProduct(shopper, product);

        if (existingReport.isPresent()) {
            Report report = existingReport.get();
            report.setRating(rating);
            report.setFeedback(feedback);
            reportRepository.save(report);
        } else {
            Report report = new Report();
            report.setShopper(shopper);
            report.setProduct(product);
            report.setRating(rating);
            report.setFeedback(feedback);
            reportRepository.save(report);
        }
    }
    public List<Report> getReportsByProductId(Long productId) {
        return reportRepository.findByProductId(productId);
    }
}
