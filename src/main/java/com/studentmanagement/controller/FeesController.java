package com.studentmanagement.controller;

import com.studentmanagement.dto.request.FeeCreateRequest;
import com.studentmanagement.dto.request.FeePaymentRequest;
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
@RequestMapping("/fees")
public class FeesController {

    private final FeesService feesService;

    public FeesController(FeesService feesService) {
        this.feesService = feesService;
    }

    @PostMapping
    public ResponseEntity<FeeResponse> createFeeRecord(
            @Valid @RequestBody FeeCreateRequest request) {
        FeeResponse created = feesService.createFeeRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/pay")
    public ResponseEntity<FeeResponse> payFees(@Valid @RequestBody FeePaymentRequest request) {
        return ResponseEntity.ok(feesService.payFees(request));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<FeeResponse>> getFeesByStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(feesService.getFeesByStudent(studentId));
    }

    @GetMapping("/student/{studentId}/pending")
    public ResponseEntity<List<FeeResponse>> getPendingFeesByStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(feesService.getPendingFeesByStudent(studentId));
    }
}
