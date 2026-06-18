package com.studentmanagement.dto.request;

import com.studentmanagement.model.enums.Gender;
import com.studentmanagement.model.enums.StudentStatus;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentUpdateRequest {

    private String firstName;
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;
    private LocalDate dateOfBirth;
    private Gender gender;
    private StudentStatus status;
    private String departmentCode;
}
