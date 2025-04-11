package com.example.businessmanagement.service;

import com.example.businessmanagement.entity.Store;
import com.example.businessmanagement.repository.AddressRepository;
import com.example.businessmanagement.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StoreService {
    private final AddressRepository addressRepository;
    private final StoreRepository storeRepository;

    public StoreService(AddressRepository addressRepository, StoreRepository storeRepository) {
        this.addressRepository = addressRepository;
        this.storeRepository = storeRepository;
    }
    // Phương thức tìm kiếm cửa hàng dựa trên tỉnh và huyện
    public List<Store> searchStores(String province, String district) {
        List<Store> stores = new ArrayList<>();
        // Nếu có cả tỉnh và huyện
        if (province != null && !province.isEmpty() && district != null && !district.isEmpty()) {
            stores = storeRepository.findByAddressProvinceAndDistrict(province, district);
        }
        // Nếu chỉ có tỉnh
        else if (province != null && !province.isEmpty()) {
            stores = storeRepository.findByStoreAddressProvinceContainingIgnoreCase(province);
        }
        // Nếu chỉ có huyện
        else if (district != null && !district.isEmpty()) {
            stores = storeRepository.findByStoreAddressDistrictContainingIgnoreCase(district);
        }
        // Nếu không có tỉnh và huyện
        else {
            stores = storeRepository.findAll();
        }
        return stores;
    }

    // Phương thức lấy danh sách các tỉnh
    public List<String> getAllProvinces() {
        return addressRepository.findAllProvinces();
    }

    // Phương thức lấy danh sách huyện theo tỉnh
    public List<String> getDistrictsByProvince(String province) {
        return addressRepository.findDistrictsByProvince(province);
    }
}
