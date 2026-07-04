package com.studentmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/page")
public class PageController {

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("title", "Dashboard — Student Management System");
        return "home";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("title", "Login — Student Management System");
        return "login";
    }

    @GetMapping("/students")
    public String studentsPage(Model model) {
        model.addAttribute("title", "Students — Student Management System");
        return "students";
    }

    @GetMapping("/faculty")
    public String facultyPage(Model model) {
        model.addAttribute("title", "Faculty — Student Management System");
        return "faculty";
    }

    @GetMapping("/courses")
    public String coursesPage(Model model) {
        model.addAttribute("title", "Courses — Student Management System");
        return "courses";
    }

    @GetMapping("/attendance")
    public String attendancePage(Model model) {
        model.addAttribute("title", "Attendance — Student Management System");
        return "attendance";
    }

    @GetMapping("/fees")
    public String feesPage(Model model) {
        model.addAttribute("title", "Fees — Student Management System");
        return "fees";
    }

    @GetMapping("/docs")
    public String apiDocs(Model model) {
        model.addAttribute("title", "API Security Guide");
        return "docs";
    }

    @GetMapping("/faculty-portal")
    public String facultyPortal(Model model) {
        model.addAttribute("title", "Faculty Dashboard — Student Management System");
        return "faculty-portal";
    }

    @GetMapping("/student-portal")
    public String studentPortal(Model model) {
        model.addAttribute("title", "Student Portal — Student Management System");
        return "student-portal";
    }
}
