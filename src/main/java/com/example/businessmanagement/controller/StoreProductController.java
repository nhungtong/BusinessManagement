package com.example.businessmanagement.controller;

import com.example.businessmanagement.entity.StoreProduct;
import com.example.businessmanagement.service.StoreProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/inventory")
public class StoreProductController {
    private final StoreProductService storeProductService;

    public StoreProductController(StoreProductService storeProductService) {
        this.storeProductService = storeProductService;
    }

    @GetMapping("/report")
    public String getInventoryReport(@RequestParam("storeId") Integer storeId, Model model) {
        List<StoreProduct> allProducts = storeProductService.getProductsByStore(storeId);
        List<StoreProduct> lowStockProducts = storeProductService.getLowStockProductsByStore(storeId);
        List<StoreProduct> overstockedProducts = storeProductService.getOverstockedProductsByStore(storeId);

        model.addAttribute("allProducts", allProducts);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("overstockedProducts", overstockedProducts);
        model.addAttribute("storeId", storeId);
        return "inventory/report";
    }
}
