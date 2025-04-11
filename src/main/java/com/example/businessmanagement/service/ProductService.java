package com.example.businessmanagement.service;

import com.example.businessmanagement.entity.Product;
import com.example.businessmanagement.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> searchProductsByName(String name) {
        return productRepository.findByProductNameContainingIgnoreCase(name);
    }
    @Transactional
    public void saveProduct(Product product, MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                String filePath = "src/main/resources/static/images/" + file.getOriginalFilename();
                File destFile = new File(filePath);

                // Kiểm tra xem ảnh đã tồn tại chưa
                if (!destFile.exists()) {
                    try (FileOutputStream fos = new FileOutputStream(destFile)) {
                        fos.write(file.getBytes());
                    }
                }

                // Gán tên file ảnh vào product
                product.setImage(file.getOriginalFilename());
            }

            // Lưu sản phẩm vào database
            productRepository.save(product);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public Product getProductById1(Long id) {
        return productRepository.findById(id).orElse(null);
    }
    public List<Product> getProductsByCategoryAndPrice(Long categoryId, Integer minPrice, Integer maxPrice) {
        return productRepository.findByCategoryIdAndPriceBetween(categoryId, minPrice, maxPrice);
    }
}
