package com.studentmanagement.controller;

import com.studentmanagement.dto.request.FacultyRegistrationRequest;
import com.studentmanagement.dto.request.FacultyUpdateRequest;
import com.studentmanagement.dto.response.ApiResponse;
import com.studentmanagement.dto.response.FacultyResponse;
import com.studentmanagement.service.FacultyService;
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
@RequestMapping("/api/v1/faculty")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FacultyResponse>> registerFaculty(
            @Valid @RequestBody FacultyRegistrationRequest request) {
        FacultyResponse response = facultyService.registerFaculty(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Faculty registered successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FacultyResponse>>> getAllFaculty() {
        return ResponseEntity.ok(ApiResponse.success(facultyService.getAllFaculty()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<FacultyResponse>>> searchFaculty(
            @RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.success(facultyService.searchFaculty(keyword)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FacultyResponse>> getFacultyById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(facultyService.findFacultyById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FacultyResponse>> updateFaculty(
            @PathVariable Long id,
            @Valid @RequestBody FacultyUpdateRequest request) {
        FacultyResponse response = facultyService.updateFaculty(id, request);
        return ResponseEntity.ok(ApiResponse.success("Faculty updated successfully", response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<FacultyResponse>> patchFaculty(
            @PathVariable Long id,
            @Valid @RequestBody FacultyUpdateRequest request) {
        FacultyResponse response = facultyService.updateFaculty(id, request);
        return ResponseEntity.ok(ApiResponse.success("Faculty partially updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteFaculty(@PathVariable Long id) {
        facultyService.deleteFacultyById(id);
        return ResponseEntity.ok(ApiResponse.success("Faculty deleted successfully"));
    }
}
