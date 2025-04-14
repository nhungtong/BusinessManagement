package com.example.businessmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart")
@Getter
@Setter

public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shopper_user_id", nullable = false)
    private User shopper;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity = 1;

    public Cart(User shopper, Product product, int quantity) {
        this.shopper = shopper;
        this.product = product;
        this.quantity = quantity;
    }

    public Cart(Long id, User shopper, Product product, int quantity) {
        this.id = id;
        this.shopper = shopper;
        this.product = product;
        this.quantity = quantity;
    }

    public Cart() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getShopper() {
        return shopper;
    }

    public void setShopper(User shopper) {
        this.shopper = shopper;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
