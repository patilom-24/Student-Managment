package com.studentmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendancePercentageResponse {

    private String studentId;
    private String courseCode;
    private double percentage;
    private long totalSessions;
    private long presentCount;
}
