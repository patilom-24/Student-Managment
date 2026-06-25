package com.studentmanagement.controller;

import com.studentmanagement.dto.request.FacultyRegistrationRequest;
import com.studentmanagement.dto.request.FacultyUpdateRequest;
import com.studentmanagement.dto.response.FacultyResponse;
import com.studentmanagement.service.FacultyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/faculty")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping
    public ResponseEntity<List<FacultyResponse>> getAllFaculty() {
        return ResponseEntity.ok(facultyService.getAllFaculty());
    }

    @GetMapping("/search")
    public ResponseEntity<List<FacultyResponse>> searchFaculty(@RequestParam String keyword) {
        return ResponseEntity.ok(facultyService.searchFaculty(keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacultyResponse> getFacultyById(@PathVariable Long id) {
        return ResponseEntity.ok(facultyService.findFacultyById(id));
    }

    @PostMapping
    public ResponseEntity<FacultyResponse> createFaculty(
            @Valid @RequestBody FacultyRegistrationRequest request) {
        FacultyResponse created = facultyService.registerFaculty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacultyResponse> updateFaculty(
            @PathVariable Long id,
            @Valid @RequestBody FacultyUpdateRequest request) {
        return ResponseEntity.ok(facultyService.updateFaculty(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaculty(@PathVariable Long id) {
        facultyService.deleteFacultyById(id);
        return ResponseEntity.noContent().build();
    }
}
