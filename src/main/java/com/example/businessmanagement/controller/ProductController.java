package com.example.businessmanagement.controller;

import com.example.businessmanagement.entity.*;
import com.example.businessmanagement.repository.UserRepository;
import com.example.businessmanagement.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReportService reportService;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final ProductSpecsService productSpecsService;

    public ProductController(ProductService productService, CategoryService categoryService, ReportService reportService, UserRepository userRepository, CartService cartService, ProductSpecsService productSpecsService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.reportService = reportService;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.productSpecsService = productSpecsService;
    }

    @GetMapping("/list")
    public String listProducts(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Product> products = (search == null || search.isEmpty())
                ? productService.getAllProducts()
                : productService.searchProductsByName(search);

        // Nếu ảnh trong database chỉ là tên file, cập nhật đường dẫn
        for (Product product : products) {
            if (product.getImage() != null && !product.getImage().startsWith("/images/")) {
                product.setImage("/images/" + product.getImage());
            }
        }

        model.addAttribute("products", products);
        return "products/list";
    }


    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products/add";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile file) {
        productService.saveProduct(product, file);
        return "redirect:/products/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id).orElseThrow(() -> new IllegalArgumentException("Invalid product ID"));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products/edit";
    }

    @PostMapping("/update")
    public String updateProduct(@ModelAttribute Product product,
                                @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            // Giữ nguyên ảnh cũ nếu không có file mới
            Product existingProduct = productService.getProductById(product.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid product ID"));
            product.setImage(existingProduct.getImage());
        }
        productService.saveProduct(product, file);
        return "redirect:/products/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/products/list";
    }
    // hiển thị chi tiết sản phẩm
    @GetMapping("/{id}")
    public String showProductDetail(@PathVariable Long id, Model model,@AuthenticationPrincipal UserDetails userDetails) {
        Product product = productService.getProductById1(id); // Lấy sản phẩm theo ID

        // Kiểm tra và thêm tiền tố "/images/" vào URL của ảnh nếu chưa có
        if (product != null && product.getImage() != null && !product.getImage().startsWith("/images/")) {
            product.setImage("/images/" + product.getImage());
        }

        // Lấy danh sách đánh giá của sản phẩm
        List<Report> reports = reportService.getReportsByProductId(id); // Lấy các đánh giá từ service
        // Lấy danh sách thông số kỹ thuật của sản phẩm
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        Long userId = user.getId();
        List<ProductSpecs> specs = productSpecsService.getSpecsByProductId(id);
        List<Cart> cartItems = cartService.getCartItems(userId);
        // Tính tổng số lượng sản phẩm trong giỏ hàng
        int cartItemCount = cartItems.stream()
                .mapToInt(Cart::getQuantity)
                .sum();



        // Thêm số lượng sản phẩm trong giỏ hàng vào model
        model.addAttribute("cartItemCount", cartItemCount);

        model.addAttribute("product", product); // Đưa sản phẩm vào model
        model.addAttribute("reports", reports); // Đưa các đánh giá vào model
        model.addAttribute("specs", specs); // Thêm vào model
        return "products/detail"; // Trả về view chi tiết sản phẩm
    }


    @GetMapping("/search")
    public String searchProducts(@RequestParam("productName") String productName, Model model) {
        List<Product> products = productService.searchProductsByName(productName);
        for (Product product : products) {
            if (product.getImage() != null && !product.getImage().startsWith("/images/")) {
                product.setImage("/images/" + product.getImage());
            }
        }
        model.addAttribute("products", products);
        model.addAttribute("productName", productName);
        return "products/searchResults";
    }
}
