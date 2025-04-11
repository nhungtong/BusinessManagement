package com.example.businessmanagement.controller;

import com.example.businessmanagement.entity.Promotion;
import com.example.businessmanagement.service.PromotionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/promotions")
public class PromotionController {
    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }
    // Hiển thị danh sách mã giảm giá, có thể tìm kiếm theo promo_code
    @GetMapping("/list")
    public String listPromotions(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Promotion> promotions = (search == null || search.isEmpty())
                ? promotionService.getAllPromotions()
                : promotionService.getPromotionsByPromoCode(search);
        model.addAttribute("promotions", promotions);
        model.addAttribute("search", search); // Giữ giá trị search khi tìm kiếm
        return "promotions/list";
    }

    // Hiển thị form thêm mã giảm giá
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("promotion", new Promotion());
        return "promotions/add";
    }

    // Xử lý thêm mã giảm giá
    @PostMapping("/add")
    public String addPromotion(@ModelAttribute Promotion promotion, Model model) {
        if (promotion.getPromoCode() == null || promotion.getPromoCode().isEmpty()) {
            model.addAttribute("error", "Mã giảm giá không được để trống!");
            model.addAttribute("promotion", promotion);
            return "promotions/add";
        }
        promotionService.savePromotion(promotion);
        return "redirect:/promotions/list";
    }

    // Hiển thị form sửa mã giảm giá
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Promotion> promotion = promotionService.getPromotionById(id);
        if (promotion.isPresent()) {
            model.addAttribute("promotion", promotion.get());
            return "promotions/edit";
        } else {
            model.addAttribute("error", "Không tìm thấy mã giảm giá!");
            return "redirect:/promotions/list";
        }
    }

    // Xử lý cập nhật mã giảm giá
    @PostMapping("/edit/{id}")
    public String updatePromotion(@PathVariable Long id, @ModelAttribute Promotion promotion, Model model) {
        if (promotion.getPromoCode() == null || promotion.getPromoCode().isEmpty()) {
            model.addAttribute("error", "Mã giảm giá không được để trống!");
            model.addAttribute("promotion", promotion);
            return "promotions/edit";
        }
        promotion.setId(id);
        promotionService.savePromotion(promotion);
        return "redirect:/promotions/list";
    }

    // Xử lý xóa mã giảm giá
    @PostMapping("/delete/{id}")
    public String deletePromotion(@PathVariable Long id, Model model) {
        Optional<Promotion> promotion = promotionService.getPromotionById(id);
        if (promotion.isPresent()) {
            promotionService.deletePromotion(id);
        }
        return "redirect:/promotions/list";
    }
}
