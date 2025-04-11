package com.example.businessmanagement.repository;

import com.example.businessmanagement.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByProvinceAndDistrictAndWardAndStreet(String province, String district, String ward, String street);

    @Query("SELECT DISTINCT a.province FROM Address a")
    List<String> findAllProvinces();

    @Query("SELECT DISTINCT a.district FROM Address a WHERE a.province = :province")
    List<String> findDistrictsByProvince(@Param("province") String province);
}
