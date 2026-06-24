package com.studentmanagement.service;

import com.studentmanagement.dto.request.FeeCreateRequest;
import com.studentmanagement.dto.request.FeePaymentRequest;
import com.studentmanagement.dto.response.FeeResponse;
import com.studentmanagement.exception.BusinessValidationException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.exception.ServiceException;
import com.studentmanagement.model.Fee;
import com.studentmanagement.model.Student;
import com.studentmanagement.model.enums.FeeStatus;
import com.studentmanagement.repository.FeeRepository;
import com.studentmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class FeesService {

    private final FeeRepository feeRepository;
    private final StudentRepository studentRepository;

    public FeesService(FeeRepository feeRepository, StudentRepository studentRepository) {
        this.feeRepository = feeRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(rollbackFor = ServiceException.class)
    public FeeResponse createFeeRecord(FeeCreateRequest request) {
        Student student = findStudentByStudentId(request.getStudentId());

        Fee fee = Fee.builder()
                .student(student)
                .feeType(request.getFeeType())
                .amount(request.getAmount())
                .paidAmount(BigDecimal.ZERO)
                .status(FeeStatus.PENDING)
                .dueDate(request.getDueDate())
                .academicYear(request.getAcademicYear())
                .semester(request.getSemester())
                .build();

        return toResponse(feeRepository.save(fee));
    }

    @Transactional(rollbackFor = ServiceException.class)
    public FeeResponse payFees(FeePaymentRequest request) {
        Fee fee = feeRepository.findById(request.getFeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Fee record not found with id: " + request.getFeeId()));

        if (fee.getStatus() == FeeStatus.PAID) {
            throw new BusinessValidationException("Fee is already fully paid");
        }

        BigDecimal newPaidAmount = fee.getPaidAmount().add(request.getPaymentAmount());

        if (newPaidAmount.compareTo(fee.getAmount()) > 0) {
            throw new BusinessValidationException("Payment amount exceeds outstanding fee balance");
        }

        fee.setPaidAmount(newPaidAmount);

        if (newPaidAmount.compareTo(fee.getAmount()) == 0) {
            fee.setStatus(FeeStatus.PAID);
            fee.setPaidDate(LocalDate.now());
        } else {
            fee.setStatus(FeeStatus.PARTIAL);
        }

        return toResponse(feeRepository.save(fee));
    }

    public List<FeeResponse> getFeesByStudent(String studentId) {
        Student student = findStudentByStudentId(studentId);
        return feeRepository.findByStudent_Id(student.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<FeeResponse> getPendingFeesByStudent(String studentId) {
        Student student = findStudentByStudentId(studentId);
        return feeRepository.findByStudent_IdAndStatus(student.getId(), FeeStatus.PENDING).stream()
                .map(this::toResponse)
                .toList();
    }

    private Student findStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with ID: " + studentId));
    }

    private FeeResponse toResponse(Fee fee) {
        BigDecimal paidAmount = fee.getPaidAmount() != null ? fee.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal outstanding = fee.getAmount().subtract(paidAmount);

        FeeResponse.FeeResponseBuilder builder = FeeResponse.builder()
                .id(fee.getId())
                .feeType(fee.getFeeType())
                .amount(fee.getAmount())
                .paidAmount(paidAmount)
                .outstandingAmount(outstanding)
                .status(fee.getStatus())
                .dueDate(fee.getDueDate())
                .paidDate(fee.getPaidDate())
                .academicYear(fee.getAcademicYear())
                .semester(fee.getSemester());

        if (fee.getStudent() != null) {
            Student student = fee.getStudent();
            builder.studentId(student.getStudentId())
                    .studentName(student.getFirstName() + " " + student.getLastName());
        }

        return builder.build();
    }
}
