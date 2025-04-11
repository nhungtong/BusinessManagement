package com.example.businessmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "address")
@Getter
@Setter
@AllArgsConstructor

public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String province;

    @Column(nullable = false, length = 50)
    private String district;

    @Column(nullable = false, length = 50)
    private String ward;

    @Column(nullable = false, length = 50)
    private String street;

    public Address(String province, String district, String ward, String street) {
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.street = street;
    }

    public Address() {
    }

    public Long getId() {
        return id;
    }

    public String getProvince() {
        return province;
    }

    public String getDistrict() {
        return district;
    }

    public String getWard() {
        return ward;
    }

    public String getStreet() {
        return street;
    }
}
