package com.studentmanagement.controller;

import com.studentmanagement.dto.request.AttendanceMarkRequest;
import com.studentmanagement.dto.response.AttendancePercentageResponse;
import com.studentmanagement.dto.response.AttendanceResponse;
import com.studentmanagement.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    public ResponseEntity<AttendanceResponse> markAttendance(
            @Valid @RequestBody AttendanceMarkRequest request) {
        AttendanceResponse marked = attendanceService.markAttendance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(marked);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByStudent(
            @PathVariable String studentId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByStudent(studentId));
    }

    @GetMapping("/course/{courseCode}")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByCourse(
            @PathVariable String courseCode) {
        return ResponseEntity.ok(attendanceService.getAttendanceByCourse(courseCode));
    }

    @GetMapping("/percentage")
    public ResponseEntity<AttendancePercentageResponse> getAttendancePercentage(
            @RequestParam String studentId,
            @RequestParam String courseCode) {
        return ResponseEntity.ok(attendanceService.getAttendancePercentage(studentId, courseCode));
    }
}
