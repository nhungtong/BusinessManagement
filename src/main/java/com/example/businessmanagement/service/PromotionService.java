package com.example.businessmanagement.service;

import com.example.businessmanagement.entity.Promotion;
import com.example.businessmanagement.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PromotionService {
    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    // Lấy danh sách tất cả mã giảm giá
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    // Tìm mã giảm giá theo ID
    public Optional<Promotion> getPromotionById(Long id) {
        return promotionRepository.findById(id);
    }

    // Tìm một mã giảm giá duy nhất theo promo_code
    public Optional<Promotion> getPromotionByPromoCode(String promoCode) {
        return promotionRepository.findByPromoCode(promoCode);
    }

    // Tìm kiếm danh sách mã giảm giá theo promo_code (tìm kiếm một phần)
    public List<Promotion> getPromotionsByPromoCode(String promoCode) {
        return promotionRepository.findByPromoCodeContainingIgnoreCase(promoCode);
    }

    // Lưu hoặc cập nhật mã giảm giá
    @Transactional
    public Promotion savePromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    // Xóa mã giảm giá theo ID
    @Transactional
    public void deletePromotion(Long id) {
        promotionRepository.deleteById(id);
    }
}

