package com.example.businessmanagement.controller;

import com.example.businessmanagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "users/list";
    }

    @GetMapping("/search")
    public String searchUser(@RequestParam String username, Model model) {
        userService.findByUsername(username).ifPresent(user -> model.addAttribute("users", List.of(user)));
        return "users/list";
    }

    @PostMapping("/deactivate/{id}")
    public String deactivateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (userService.deactivateUser(id)) {
            redirectAttributes.addFlashAttribute("success", "Tài khoản đã bị khóa!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy tài khoản!");
        }
        return "redirect:/users/list";
    }

    @PostMapping("/activate/{id}")
    public String activateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (userService.activateUser(id)) {
            redirectAttributes.addFlashAttribute("success", "Tài khoản đã được mở khóa!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy tài khoản!");
        }
        return "redirect:/users/list";
    }
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (userService.deleteUser(id)) {
            redirectAttributes.addFlashAttribute("success", "Tài khoản đã bị xóa!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy tài khoản!");
        }
        return "redirect:/users/list";
    }
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return "redirect:/";
    }
}
