package com.studentmanagement.dto.response;

import com.studentmanagement.model.enums.FeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeResponse {

    private Long id;
    private String studentId;
    private String studentName;
    private String feeType;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private FeeStatus status;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private String academicYear;
    private String semester;
}
