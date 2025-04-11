package com.example.businessmanagement.controller;

import com.example.businessmanagement.entity.Category;
import com.example.businessmanagement.entity.Product;
import com.example.businessmanagement.service.CategoryService;
import com.example.businessmanagement.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final ProductService productService;

    public CategoryController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    // Hiển thị danh sách danh mục
    @GetMapping("/list")
    public String listCategories(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<Category> categories = (keyword == null || keyword.isEmpty())
                ? categoryService.getAllCategories()
                : categoryService.searchCategories(keyword);
        model.addAttribute("categories", categories);
        return "categories/list";
    }

    // Hiển thị form thêm danh mục
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        return "categories/add";
    }
    // Xử lý thêm danh mục
    @PostMapping("/add")
    public String saveCategory(@ModelAttribute Category category) {
        categoryService.saveCategory(category);
        return "redirect:/categories/list";
    }

    // Hiển thị form sửa danh mục
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Category> category = categoryService.getCategoryById(id);
        if (category.isPresent()) {
            model.addAttribute("category", category.get());
            return "categories/edit";
        } else {
            return "redirect:/categories/list";
        }
    }

    // Xử lý cập nhật danh mục
    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable Long id, @ModelAttribute Category category) {
        Category existingCategory = categoryService.getCategoryById(id).orElseThrow();
        existingCategory.setCategoryName(category.getCategoryName()); // Chỉ cập nhật tên
        categoryService.saveCategory(existingCategory);
        return "redirect:/categories/list";
    }

    // Xóa danh mục
    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/categories/list";
    }

    // Xem sản phẩm thuộc danh mục
    @GetMapping("/{id}/products_detail")
    public String viewProductsByCategory(@PathVariable Long id, Model model) {
        Optional<Category> category = categoryService.getCategoryById(id);
        if (category.isPresent()) {
            List<Product> products = category.get().getProducts();
            model.addAttribute("category", category.get());
            model.addAttribute("products", products);
            return "categories/products_detail";
        } else {
            return "redirect:/categories/list";
        }
    }
    // bảng danh mục ở index.html
    @GetMapping("/shopper_list")
    public String showCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "categories/shopper_list";
    }
    // sẽ hiển thị toàn bộ sản phẩm thuộc một danh mục
    @GetMapping("/{id}/products")
    public String showProductsByCategory(@PathVariable Long id, Model model) {
        List<Product> products = productService.getProductsByCategory(id);
        model.addAttribute("products", products);
        return "categories/detail";
    }
    @GetMapping("/{categoryId}")
    public String getProductsByCategory(@PathVariable("categoryId") Long categoryId, Model model) {
        List<Product> products = productService.getProductsByCategory(categoryId);
        List<Category> categories = categoryService.getAllCategories();

        // Cập nhật đường dẫn ảnh nếu chỉ lưu tên file
        for (Product product : products) {
            if (product.getImage() != null && !product.getImage().startsWith("/images/")) {
                product.setImage("/images/" + product.getImage());
            }
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryId", categoryId);
        return "index";
    }
    // Xem sản phẩm theo danh mục và lọc theo giá (Shopper)
    @GetMapping("/{id}/shopper/products")
    public String viewProductsForShopper(@PathVariable Long id,
                                         @RequestParam(value = "priceRange", required = false) String priceRange,
                                         Model model) {
        Optional<Category> categoryOpt = categoryService.getCategoryById(id);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            List<Product> allProducts = category.getProducts();
            List<Product> filteredProducts;

            if (priceRange != null && priceRange.contains("-")) {
                String[] parts = priceRange.split("-");
                int minPrice = Integer.parseInt(parts[0]);
                int maxPrice = Integer.parseInt(parts[1]);

                filteredProducts = allProducts.stream()
                        .filter(product -> {
                            int finalPrice;
                            if (product.getDiscount() > 0) {
                                finalPrice = (int) (product.getPrice() * (100 - product.getDiscount()) / 100.0);
                            } else {
                                finalPrice = product.getPrice();
                            }
                            return finalPrice >= minPrice && finalPrice <= maxPrice;
                        })
                        .collect(Collectors.toList());
            } else {
                filteredProducts = allProducts;
            }

            // Xử lý hình ảnh
            for (Product product : filteredProducts) {
                if (product.getImage() != null && !product.getImage().startsWith("/images/")) {
                    product.setImage("/images/" + product.getImage());
                }
            }

            model.addAttribute("category", category);
            model.addAttribute("products", filteredProducts);
            return "categories/user_products";
        } else {
            return "redirect:/index";
        }
    }
}
