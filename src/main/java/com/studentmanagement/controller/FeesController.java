package com.studentmanagement.controller;

import com.studentmanagement.dto.request.FeeCreateRequest;
import com.studentmanagement.dto.request.FeePaymentRequest;
import com.studentmanagement.dto.response.ApiResponse;
import com.studentmanagement.dto.response.FeeResponse;
import com.studentmanagement.service.FeesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fees")
public class FeesController {

    private final FeesService feesService;

    public FeesController(FeesService feesService) {
        this.feesService = feesService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FeeResponse>> createFeeRecord(
            @Valid @RequestBody FeeCreateRequest request) {
        FeeResponse response = feesService.createFeeRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fee record created successfully", response));
    }

    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<FeeResponse>> payFees(
            @Valid @RequestBody FeePaymentRequest request) {
        FeeResponse response = feesService.payFees(request);
        return ResponseEntity.ok(ApiResponse.success("Fee payment recorded successfully", response));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<FeeResponse>>> getFeesByStudent(
            @PathVariable String studentId) {
        return ResponseEntity.ok(ApiResponse.success(feesService.getFeesByStudent(studentId)));
    }

    @GetMapping("/student/{studentId}/pending")
    public ResponseEntity<ApiResponse<List<FeeResponse>>> getPendingFeesByStudent(
            @PathVariable String studentId) {
        return ResponseEntity.ok(ApiResponse.success(feesService.getPendingFeesByStudent(studentId)));
    }
}
