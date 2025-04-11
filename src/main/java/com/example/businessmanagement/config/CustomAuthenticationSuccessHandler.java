package com.example.businessmanagement.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
@Override
public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                    Authentication authentication) throws IOException {
    String role = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .findFirst()
            .orElse("");

    if ("ROLE_SHOPPER".equals(role)) {
        String referer = request.getHeader("Referer"); // Lấy trang trước đó
        if (referer != null && !referer.contains("/login")) {
            response.sendRedirect(referer); // Quay lại trang trước khi đăng nhập
            return;
        }
        response.sendRedirect("/"); // Nếu không có referer, chuyển về trang chủ
        return;
    }

    // Điều hướng mặc định cho admin và shipper
    switch (role) {
        case "ROLE_ADMIN" -> response.sendRedirect("/admin");
        case "ROLE_SHIPPER" -> response.sendRedirect("/shipper");
        default -> response.sendRedirect("/");
    }
}
}
