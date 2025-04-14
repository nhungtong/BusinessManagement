package com.example.businessmanagement.service;

import com.example.businessmanagement.entity.Cart;
import com.example.businessmanagement.entity.Product;
import com.example.businessmanagement.entity.Store;
import com.example.businessmanagement.entity.User;
import com.example.businessmanagement.repository.CartRepository;
import com.example.businessmanagement.repository.ProductRepository;
import com.example.businessmanagement.repository.StoreProductRepository;
import com.example.businessmanagement.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    private final CartRepository cartRepository;

    private final UserRepository userRepository;

    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }
    public void updateQuantity(Long cartId, int quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));
        cart.setQuantity(quantity);
        cartRepository.save(cart);
    }
    public void addToCart(Long userId, Long productId, int quantity) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Cart cartItem = cartRepository.findByShopperAndProduct(user, product)
                .orElse(null);

        if (cartItem != null) {

            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {

            cartItem = new Cart(user, product, quantity);
        }

        cartRepository.save(cartItem);
    }
    public List<Cart> getCartItems(Long userId) {
        User shopper = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return cartRepository.findByShopper(shopper);
    }
    public void removeFromCart(Long cartId) {
        cartRepository.deleteById(cartId);
    }
    public int calculateTotalAmount(List<Cart> cartItems) {
        int total = 0;
        for (Cart item : cartItems) {
            int price = item.getProduct().getFinalPrice();
            total += price * item.getQuantity();
        }
        return total;
    }
    public List<Cart> getCartItemsByIds(List<Long> ids, String username) {
        return cartRepository.findByIdInAndShopper_Username(ids, username);
    }
}
