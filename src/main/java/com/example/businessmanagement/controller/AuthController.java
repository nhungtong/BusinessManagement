package com.example.businessmanagement.controller;

import com.example.businessmanagement.dto.LoginDTO;
import com.example.businessmanagement.dto.RegisterDTO;
import com.example.businessmanagement.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("user") LoginDTO loginDTO, Model model) {
        String role = authService.login(loginDTO);

        switch (role) {
            case "ADMIN":
                return "redirect:/admin";
            case "SHOPPER":
                return "redirect:/home";
            case "SHIPPER":
                return "redirect:/delivery";
            default:
                model.addAttribute("error", "Sai tài khoản hoặc mật khẩu!");
                return "auth/login";
        }
    }


    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") RegisterDTO registerDTO, Model model) {
        try {
            authService.register(registerDTO);
            model.addAttribute("success", "Đăng ký thành công!");
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi đăng ký: " + e.getMessage());
            return "auth/register";
        }
    }

}
