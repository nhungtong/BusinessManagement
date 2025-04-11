package com.example.businessmanagement.dto;

public class TopSellingProductDto {

    private Long productId;
    private Long totalQuantitySold;

    public TopSellingProductDto(Long productId, Long totalQuantitySold) {
        this.productId = productId;
        this.totalQuantitySold = totalQuantitySold;
    }

    // Getters và setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getTotalQuantitySold() {
        return totalQuantitySold;
    }

    public void setTotalQuantitySold(Long totalQuantitySold) {
        this.totalQuantitySold = totalQuantitySold;
    }
}

