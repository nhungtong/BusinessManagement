package com.example.businessmanagement.controller;

import com.example.businessmanagement.dto.CartItemRequest;
import com.example.businessmanagement.entity.Cart;
import com.example.businessmanagement.entity.User;
import com.example.businessmanagement.repository.UserRepository;
import com.example.businessmanagement.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        if (principal == null) {
            // Nếu chưa đăng nhập, chuyển hướng về trang đăng nhập
            return "redirect:/login";
        }

        try {
            String username = principal.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            cartService.addToCart(user.getId(), productId, quantity);

            redirectAttributes.addFlashAttribute("successMessage", "Thêm vào giỏ hàng thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/products/" + productId;
    }
    @PostMapping("/update-quantity")
    public String updateQuantity(@RequestParam("cartId") Long cartId,
                                 @RequestParam("quantity") int quantity,
                                 Model model, Principal principal) {
        if (quantity < 1) {
            quantity = 1;
        }

        // Cập nhật số lượng trong cơ sở dữ liệu
        cartService.updateQuantity(cartId, quantity);

        // Lấy dữ liệu giỏ hàng và hiển thị lại view
        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        Long userId = user.getId();

        List<Cart> cartItems = cartService.getCartItems(userId)
                .stream()
                .sorted(Comparator.comparing(Cart::getId)) // Sắp theo ID tăng dần
                .collect(Collectors.toList());
        double totalPrice = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getFinalPrice() * item.getQuantity())
                .sum();

        model.addAttribute("userId", userId);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);

        // Truyền tham số ID của dòng cập nhật vào URL
        return "redirect:/cart/view";
    }

    @GetMapping("/view")
    public String showCart(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";  // Nếu người dùng chưa đăng nhập
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Long userId = user.getId();

        List<Cart> cartItems = cartService.getCartItems(userId)
                .stream()
                .sorted(Comparator.comparing(Cart::getId)) // Sắp theo ID tăng dần
                .collect(Collectors.toList());
        int cartItemCount = cartItems.stream()
                .mapToInt(Cart::getQuantity)
                .sum();  // Tổng số lượng sản phẩm (nếu có quantity)

        // Tính tổng tiền giỏ hàng
        double totalPrice = 0.0;
        for (Cart item : cartItems) {
            totalPrice += item.getProduct().getFinalPrice() * item.getQuantity();
        }

        model.addAttribute("userId", userId);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartItemCount", cartItemCount); // Tổng số lượng sản phẩm
        model.addAttribute("totalPrice", totalPrice);  // Tổng tiền

        return "cart/view";
    }
    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long cartId, @RequestParam Long userId) {
        cartService.removeFromCart(cartId);
        return "redirect:/cart/view?userId=" + userId;
    }
}
