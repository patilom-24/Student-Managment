package com.studentmanagement.controller;

import com.studentmanagement.dto.response.ApiResponse;
import com.studentmanagement.model.Department;
import com.studentmanagement.repository.DepartmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    /**
     * GET /api/v1/departments
     * Returns all departments ordered by name.
     * Used to populate dropdown lists in the frontend.
     * Response: [ { "code": "CS", "name": "Computer Science" }, ... ]
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getAllDepartments() {
        List<Map<String, String>> departments = departmentRepository
                .findAllByOrderByNameAsc()
                .stream()
                .map(d -> {
                    Map<String, String> map = new LinkedHashMap<>();
                    map.put("code", d.getCode());
                    map.put("name", d.getName());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(departments));
    }
}
