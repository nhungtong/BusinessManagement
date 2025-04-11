package com.example.businessmanagement.repository;

import com.example.businessmanagement.entity.Store;
import com.example.businessmanagement.entity.StoreProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreProductRepository extends JpaRepository<StoreProduct, Integer> {

    // Lấy danh sách sản phẩm của một cửa hàng cụ thể
    @Query("SELECT sp FROM StoreProduct sp WHERE sp.store.id = :storeId")
    List<StoreProduct> getProductsByStore(@Param("storeId") Integer storeId);

    // Lọc sản phẩm sắp hết hàng (số lượng < 10)
    @Query("SELECT sp FROM StoreProduct sp WHERE sp.store.id = :storeId AND sp.quantity < 10")
    List<StoreProduct> getLowStockProductsByStore(@Param("storeId") Integer storeId);

    // Lọc sản phẩm dư thừa (số lượng > 100)
    @Query("SELECT sp FROM StoreProduct sp WHERE sp.store.id = :storeId AND sp.quantity > 100")
    List<StoreProduct> getOverstockedProductsByStore(@Param("storeId") Integer storeId);

    @Query("SELECT sp.store FROM StoreProduct sp WHERE sp.product.id = :productId AND sp.store.id = :storeId")
    StoreProduct findByProductId(Long productId);
}