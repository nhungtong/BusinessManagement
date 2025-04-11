package com.example.businessmanagement.service;

import com.example.businessmanagement.repository.ProductSpecsRepository;
import org.springframework.stereotype.Service;
import com.example.businessmanagement.entity.ProductSpecs;

import java.util.List;

@Service
public class ProductSpecsService {
    private final ProductSpecsRepository productSpecsRepository;

    public ProductSpecsService(ProductSpecsRepository productSpecsRepository) {
        this.productSpecsRepository = productSpecsRepository;
    }

    public List<ProductSpecs> getSpecsByProductId(Long productId) {
        return productSpecsRepository.findByProductIdOrderByDisplayOrderAsc(productId);
    }
}
