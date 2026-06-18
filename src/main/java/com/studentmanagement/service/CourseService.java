package com.studentmanagement.service;

import com.studentmanagement.dto.request.CourseCreateRequest;
import com.studentmanagement.model.Course;

import java.util.List;

public interface CourseService {

    Course createCourse(CourseCreateRequest request);

    Course updateCourse(Long id, CourseCreateRequest request);

    void deleteCourseById(Long id);

    Course findCourseById(Long id);

    Course findCourseByCode(String courseCode);

    List<Course> searchCourse(String keyword);

    Course assignInstructorToCourse(String courseCode, String instructorEmployeeId);
}
