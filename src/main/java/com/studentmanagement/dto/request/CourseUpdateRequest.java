package com.studentmanagement.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseUpdateRequest {

    private String courseCode;
    private String courseName;
    private String description;

    @Min(value = 1, message = "Credits must be at least 1")
    private Integer credits;

    private String semester;
    private String academicYear;
    private String departmentCode;
    private String instructorEmployeeId;
}
