package com.studentmanagement.dto.response;

import com.studentmanagement.model.enums.Gender;
import com.studentmanagement.model.enums.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {

    private Long id;
    private String studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private Gender gender;
    private StudentStatus status;
    private LocalDate enrollmentDate;
    private String departmentCode;
    private String departmentName;
}
