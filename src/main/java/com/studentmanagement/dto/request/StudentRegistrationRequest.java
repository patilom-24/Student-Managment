package com.studentmanagement.dto.request;

import com.studentmanagement.model.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRegistrationRequest {

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Mobile number is required")
    private String phone;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private Gender gender;

    @NotBlank(message = "Department/Batch code is required")
    private String departmentCode;

    @NotBlank(message = "Course code is required")
    private String courseCode;

    @NotBlank(message = "Semester is required")
    private String semester;

    @NotBlank(message = "Academic year is required")
    private String academicYear;
}
