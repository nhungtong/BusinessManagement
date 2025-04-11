package com.example.businessmanagement.service;

import com.example.businessmanagement.repository.AnalyticRepository;
import org.springframework.stereotype.Service;

@Service
public class AnalyticService {
    private final AnalyticRepository analyticRepository;

    public AnalyticService(AnalyticRepository analyticRepository) {
        this.analyticRepository = analyticRepository;
    }
}
