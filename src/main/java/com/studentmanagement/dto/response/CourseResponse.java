package com.studentmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {

    private Long id;
    private String courseCode;
    private String courseName;
    private String description;
    private Integer credits;
    private String semester;
    private String academicYear;
    private String departmentCode;
    private String departmentName;
    private String instructorEmployeeId;
    private String instructorName;
}
