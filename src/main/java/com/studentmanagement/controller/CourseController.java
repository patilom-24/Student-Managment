package com.studentmanagement.controller;

import com.studentmanagement.dto.request.AssignInstructorRequest;
import com.studentmanagement.dto.request.CourseCreateRequest;
import com.studentmanagement.dto.request.CourseUpdateRequest;
import com.studentmanagement.dto.response.ApiResponse;
import com.studentmanagement.dto.response.CourseResponse;
import com.studentmanagement.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @Valid @RequestBody CourseCreateRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCourses() {
        return ResponseEntity.ok(ApiResponse.success(courseService.getAllCourses()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> searchCourses(
            @RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.success(courseService.searchCourse(keyword)));
    }

    @GetMapping("/code/{courseCode}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseByCode(
            @PathVariable String courseCode) {
        return ResponseEntity.ok(ApiResponse.success(courseService.findCourseByCode(courseCode)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(courseService.findCourseById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseUpdateRequest request) {
        CourseResponse response = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully", response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> patchCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseUpdateRequest request) {
        CourseResponse response = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponse.success("Course partially updated successfully", response));
    }

    @PutMapping("/assign-instructor")
    public ResponseEntity<ApiResponse<CourseResponse>> assignInstructor(
            @Valid @RequestBody AssignInstructorRequest request) {
        CourseResponse response = courseService.assignInstructorToCourse(request);
        return ResponseEntity.ok(ApiResponse.success("Instructor assigned successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourseById(id);
        return ResponseEntity.ok(ApiResponse.success("Course deleted successfully"));
    }
}
