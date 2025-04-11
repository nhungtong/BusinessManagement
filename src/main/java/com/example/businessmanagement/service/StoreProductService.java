package com.example.businessmanagement.service;

import com.example.businessmanagement.entity.StoreProduct;
import com.example.businessmanagement.repository.StoreProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreProductService {
    private final StoreProductRepository storeProductRepository;

    public StoreProductService(StoreProductRepository storeProductRepository) {
        this.storeProductRepository = storeProductRepository;
    }

    // Lấy tất cả sản phẩm theo cửa hàng
    public List<StoreProduct> getProductsByStore(Integer storeId) {
        return storeProductRepository.getProductsByStore(storeId);
    }

    // Lấy danh sách sản phẩm sắp hết hàng
    public List<StoreProduct> getLowStockProductsByStore(Integer storeId) {
        return storeProductRepository.getLowStockProductsByStore(storeId);
    }

    // Lấy danh sách sản phẩm dư thừa
    public List<StoreProduct> getOverstockedProductsByStore(Integer storeId) {
        return storeProductRepository.getOverstockedProductsByStore(storeId);
    }
}
