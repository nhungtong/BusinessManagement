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

//     Kiểm tra và mã hóa mật khẩu nếu chưa mã hóa khi ứng dụng khởi động
    @PostConstruct
    public void encryptExistingPasswords() {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            String password = user.getPassword();
            if (!password.startsWith("$2a$")) { // Kiểm tra nếu mật khẩu chưa mã hóa (BCrypt bắt đầu bằng "$2a$")
                String encodedPassword = passwordEncoder.encode(password);
                user.setPassword(encodedPassword);
                userRepository.save(user);
            }
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

        Role role = roleRepository.findByRoleName(dto.getRolename())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        User user = new User(null, dto.getUsername(),encodedPassword,
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
