//package com.example.demo.user.admin.panel;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/admin")
//public class AdminController {
//
//    @GetMapping("/dashboard")
//    public String adminDashboard(Authentication authentication) {
//        if (hasAdminRole(authentication)) {
//            return "admin/dashboard";
//        }
//        return "redirect:/access-denied";
//    }
//
//    private boolean hasAdminRole(Authentication authentication) {
//        return authentication.getAuthorities().stream()
//                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
//    }
//
//    @GetMapping("/users")
//    public String manageUsers(Model model) {
//        model.addAttribute("users", userService.getAllUsers());
//        return "admin/users";
//    }
//
//}