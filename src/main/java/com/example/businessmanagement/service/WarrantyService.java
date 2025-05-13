package com.example.businessmanagement.service;

import com.example.businessmanagement.entity.Warranty;
import com.example.businessmanagement.repository.WarrantyRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WarrantyService {
    private final WarrantyRepository warrantyRepository;

    public WarrantyService(WarrantyRepository warrantyRepository) {
        this.warrantyRepository = warrantyRepository;
    }

    public Optional<Warranty> getWarrantyByOrderId(Long orderId) {
        return warrantyRepository.findByOrder_Id(orderId);
    }
}
