package com.studentmanagement.service;

import com.studentmanagement.dto.request.FeeCreateRequest;
import com.studentmanagement.dto.request.FeePaymentRequest;
import com.studentmanagement.model.Fee;

import java.util.List;

public interface FeesService {

    Fee createFeeRecord(FeeCreateRequest request);

    Fee payFees(FeePaymentRequest request);

    List<Fee> getFeesByStudent(String studentId);

    List<Fee> getPendingFeesByStudent(String studentId);
}
