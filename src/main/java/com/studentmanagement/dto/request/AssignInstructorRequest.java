package com.studentmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignInstructorRequest {

    @NotBlank(message = "Course code is required")
    private String courseCode;

    @NotBlank(message = "Instructor employee ID is required")
    private String instructorEmployeeId;
}
