package com.example.businessmanagement.dto;

import java.util.List;

public class ReviewForm {
    private List<ReportDTO> reviews;

    public List<ReportDTO> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReportDTO> reviews) {
        this.reviews = reviews;
    }
}

