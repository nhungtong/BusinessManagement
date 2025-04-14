package com.example.businessmanagement.controller;

import com.example.businessmanagement.dto.RevenueReportDto;
import com.example.businessmanagement.dto.TopSellingProductDto;
import com.example.businessmanagement.service.AnalyticService;
import com.example.businessmanagement.service.OrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/analytic")
public class AnalyticController {
    private final OrderService orderService;

    public AnalyticController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/list")
    public String showAnalytic() {
        return "analytic/list";
    }
    @GetMapping("/revenue")
    public String revenueReport(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                Model model) {
        if (startDate == null || endDate == null) {
            startDate = LocalDate.now().minusMonths(6).withDayOfMonth(1);
            endDate = LocalDate.now();
        }

        Date startDateDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDateDate = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<RevenueReportDto> reports = orderService.getRevenueBetween(startDateDate, endDateDate);
        if (reports.isEmpty()) {
            System.out.println("No revenue data found.");
        } else {
            System.out.println("Revenue data retrieved successfully.");
        }
        List<String> labels = reports.stream()
                .map(report -> report.getYear() + "-" + report.getMonth())
                .collect(Collectors.toList());

        List<Long> data = reports.stream()
                .map(RevenueReportDto::getTotalRevenue)
                .collect(Collectors.toList());
        System.out.println("Labels: " + labels);
        System.out.println("Data: " + data);
        model.addAttribute("reports", reports);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("labels", labels);
        model.addAttribute("data", data);

        return "analytic/monthly_revenue";
    }
    @GetMapping("/top-selling-products")
    public String topSellingProductsReport(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                           Model model) {
        if (startDate == null || endDate == null) {
            // Nếu không có ngày đầu và ngày cuối, mặc định lấy 1 tháng trước
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.MONTH, -1);
            startDate = calendar.getTime();
            endDate = new Date();
        }

        List<TopSellingProductDto> topSellingProducts = orderService.getTopSellingProducts(startDate, endDate);

        model.addAttribute("topSellingProducts", topSellingProducts);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "analytic/top_selling_products";
    }
}
