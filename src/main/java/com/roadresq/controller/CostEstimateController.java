package com.roadresq.controller;

import com.roadresq.dto.CostEstimateRequestDto;
import com.roadresq.dto.CostEstimateResponseDto;
import com.roadresq.service.CostEstimatorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cost-estimate")
public class CostEstimateController {

    private final CostEstimatorService costEstimatorService;

    public CostEstimateController(CostEstimatorService costEstimatorService) {
        this.costEstimatorService = costEstimatorService;
    }

    @PostMapping
    public ResponseEntity<CostEstimateResponseDto> calculateEstimate(@Valid @RequestBody CostEstimateRequestDto request) {
        return ResponseEntity.ok(costEstimatorService.calculateEstimate(request));
    }
}
