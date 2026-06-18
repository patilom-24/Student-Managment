package com.studentmanagement.dto.request;

import com.studentmanagement.model.enums.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceMarkRequest {

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "Course code is required")
    private String courseCode;

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    @NotNull(message = "Attendance status is required")
    private AttendanceStatus status;

    private String remarks;
}
