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
        model.addAttribute("title", "Student Management System");
        model.addAttribute("apiBaseUrl", "/api/v1");
        return "home";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("title", "Login");
        model.addAttribute("loginUrl", "/api/v1/auth/login");
        return "login";
    }

    @GetMapping("/docs")
    public String apiDocs(Model model) {
        model.addAttribute("title", "API Security Guide");
        return "docs";
    }
}
