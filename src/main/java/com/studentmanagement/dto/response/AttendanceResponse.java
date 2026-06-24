package com.studentmanagement.dto.response;

import com.studentmanagement.model.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponse {

    private Long id;
    private String studentId;
    private String studentName;
    private String courseCode;
    private String courseName;
    private LocalDate attendanceDate;
    private AttendanceStatus status;
    private String remarks;
}
