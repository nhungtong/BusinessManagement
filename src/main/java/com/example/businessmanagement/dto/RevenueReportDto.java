package com.example.businessmanagement.dto;

import lombok.Data;

@Data
public class RevenueReportDto {
    private int month;
    private int year;
    private Long totalRevenue;

    public RevenueReportDto(int month, int year, Long totalRevenue) {
        this.month = month;
        this.year = year;
        this.totalRevenue = totalRevenue;
    }

    public String getMonthYear() {
        return String.format("%02d/%d", month, year);
    }

    public Long getTotalRevenue() {
        return totalRevenue;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setTotalRevenue(Long totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

}
