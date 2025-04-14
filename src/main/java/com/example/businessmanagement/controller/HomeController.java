package com.example.businessmanagement.controller;

import com.example.businessmanagement.entity.Cart;
import com.example.businessmanagement.entity.Category;
import com.example.businessmanagement.entity.Product;
import com.example.businessmanagement.entity.User;
import com.example.businessmanagement.repository.UserRepository;
import com.example.businessmanagement.service.CartService;
import com.example.businessmanagement.service.CategoryService;
import com.example.businessmanagement.service.ProductService;
import com.example.businessmanagement.service.StoreService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final UserRepository userRepository;
    private final CartService cartService;

    public HomeController(CategoryService categoryService, ProductService productService, UserRepository userRepository, CartService cartService) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.userRepository = userRepository;
        this.cartService = cartService;
    }

    @GetMapping("/")
    public String homePage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            model.addAttribute("loggedInUser", userDetails.getUsername());

            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            Long userId = user.getId();

            List<Cart> cartItems = cartService.getCartItems(userId);
            int cartItemCount = cartItems.stream()
                    .mapToInt(Cart::getQuantity)
                    .sum();

            model.addAttribute("cartItemCount", cartItemCount);
        }

        List<Category> categories = categoryService.getAllCategories();
        List<Product> products = productService.getAllProducts();

        // Cập nhật đường dẫn ảnh cho sản phẩm nếu chỉ lưu tên file
        for (Product product : products) {
            if (product.getImage() != null && !product.getImage().startsWith("/images/")) {
                product.setImage("/images/" + product.getImage());
            }
        }

        model.addAttribute("categories", categories);
        model.addAttribute("products", products);

        return "index";
    }

    @GetMapping("/header")
    public String header() {
        return "fragments/Header";
    }
    @GetMapping("/admin")
    public String adminDashboard() {
        return "admin";
    }

    @GetMapping("/shopper")
    public String shopperDashboard() {
        return "index";
    }

    @GetMapping("/shipper")
    public String shipperDashboard() {
        return "delivery";
    }
}

