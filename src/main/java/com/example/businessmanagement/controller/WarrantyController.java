package com.example.businessmanagement.controller;

import com.example.businessmanagement.entity.Warranty;
import com.example.businessmanagement.service.WarrantyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/warranty")
public class WarrantyController {
    private final WarrantyService warrantyService;

    public WarrantyController(WarrantyService warrantyService) {
        this.warrantyService = warrantyService;
    }

    @GetMapping("/check-warranty")
    public String showWarrantyLookupPage(@RequestParam(value = "orderId", required = false) Long orderId,
                                         Model model) {
        if (orderId != null) {
            Optional<Warranty> warranty = warrantyService.getWarrantyByOrderId(orderId);
            if (warranty.isPresent()) {
                model.addAttribute("warranty", warranty.get());
            } else {
                model.addAttribute("notFound", true);
            }
        }
        return "warranty/check-warranty";
    }
}
