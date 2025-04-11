package com.example.businessmanagement.service;

import com.example.businessmanagement.dto.LoginDTO;
import com.example.businessmanagement.dto.RegisterDTO;
import com.example.businessmanagement.entity.Role;
import com.example.businessmanagement.entity.User;
import com.example.businessmanagement.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Setter
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Kiểm tra và mã hóa mật khẩu nếu chưa mã hóa khi ứng dụng khởi động
    @PostConstruct
    public void encryptExistingPasswords() {
        try {
            List<User> users = userRepository.findAll(); // Lấy danh sách tất cả người dùng

            for (User user : users) {
                String password = user.getPassword();
                if (password != null && !password.startsWith("$2a$")) {
                    String encodedPassword = passwordEncoder.encode(password);
                    user.setPassword(encodedPassword);
                    userRepository.save(user);
                    System.out.println("Đã mã hóa mật khẩu cho user: " + user.getUsername());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String register(RegisterDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            return "Username đã tồn tại!";
        }
        if (userRepository.findByPhoneNumber(dto.getPhonenumber()).isPresent()) {
            return "Phone number đã tồn tại!";
        }
        if (dto.getEmail() != null && userRepository.findByEmail(dto.getEmail()).isPresent()) {
            return "Email đã tồn tại!";
        }

        // Tìm role theo tên từ combobox
        Role role = roleRepository.findByRoleName(dto.getRolename())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        // Mã hóa mật khẩu trước khi lưu
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        User user = new User(null, dto.getUsername(), encodedPassword,
                dto.getPhonenumber(), dto.getEmail(), dto.getDob(), role, true);
        userRepository.save(user);

        return "Người dùng đăng ký thành công!";
    }

    public String login(LoginDTO dto) {
        Optional<User> userOpt = userRepository.findByUsername(dto.getUsername());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                return user.getRole().getRoleName(); // Gọi đúng roleName từ Role
            }
        }
        return "FAIL";
    }
}
