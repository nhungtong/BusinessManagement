package com.example.businessmanagement.controller;

import com.example.businessmanagement.entity.Cart;
import com.example.businessmanagement.entity.ProductSpecs;
import com.example.businessmanagement.entity.Store;
import com.example.businessmanagement.entity.User;
import com.example.businessmanagement.repository.UserRepository;
import com.example.businessmanagement.service.CartService;
import com.example.businessmanagement.service.ProductSpecsService;
import com.example.businessmanagement.service.StoreService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/stores")
public class StoreController {
    private final StoreService storeService;
    private final UserRepository userRepository;
    private final CartService cartService;

    public StoreController(StoreService storeService, UserRepository userRepository, CartService cartService) {
        this.storeService = storeService;
        this.userRepository = userRepository;
        this.cartService = cartService;
    }

    @GetMapping("/search")
    public String searchStores(@RequestParam(required = false) String province,
                               @RequestParam(required = false) String district,
                               Model model,@AuthenticationPrincipal UserDetails userDetails) {

        List<String> provinces = storeService.getAllProvinces();

        List<Store> stores = storeService.searchStores(province, district);
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        Long userId = user.getId();

        List<Cart> cartItems = cartService.getCartItems(userId);

        int cartItemCount = cartItems.stream()
                .mapToInt(Cart::getQuantity)
                .sum();

        model.addAttribute("cartItemCount", cartItemCount);
        model.addAttribute("provinces", provinces);
        model.addAttribute("stores", stores);
        model.addAttribute("province", province);
        model.addAttribute("district", district);

        return "stores/search";
    }

}
