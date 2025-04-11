package com.example.businessmanagement.repository;

import com.example.businessmanagement.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    boolean existsByStoreAddressId(Long addressId);
    // Tìm các cửa hàng theo tỉnh và huyện
    @Query("SELECT s FROM Store s JOIN Address a ON s.storeAddress.id = a.id WHERE a.province LIKE %:province% AND a.district LIKE %:district%")
    List<Store> findByAddressProvinceAndDistrict(@Param("province") String province, @Param("district") String district);

    // Tìm các cửa hàng theo tỉnh
    List<Store> findByStoreAddressProvinceContainingIgnoreCase(String province);

    // Tìm các cửa hàng theo huyện
    List<Store> findByStoreAddressDistrictContainingIgnoreCase(String district);

    @Query("SELECT s FROM Store s WHERE s.id = :storeId")
    Store findStoreById(@Param("storeId") Long id);

}
