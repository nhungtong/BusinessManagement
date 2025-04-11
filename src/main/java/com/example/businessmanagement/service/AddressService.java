package com.example.businessmanagement.service;

import com.example.businessmanagement.entity.Address;
import com.example.businessmanagement.repository.AddressRepository;
import com.example.businessmanagement.repository.OrderRepository;
import com.example.businessmanagement.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {
    private final AddressRepository addressRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;

    public AddressService(AddressRepository addressRepository, StoreRepository storeRepository, OrderRepository orderRepository) {
        this.addressRepository = addressRepository;
        this.storeRepository = storeRepository;
        this.orderRepository = orderRepository;
    }

    public List<Address> getAllAddresses() {
        return addressRepository.findAll();
    }

    public boolean canDeleteAddress(Long id) {
        return !storeRepository.existsByStoreAddressId(id) && !orderRepository.existsByAddressEndId(id);
    }

    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }
}
