package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.model.Product;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    Product addProduct(Product product);

    List<Product> getAllProducts();

    Optional<Product> getProductById(Long id);

    Product updateProduct(Long id, Product product);

    void deleteProduct(Long id);

    List<Product> searchProducts(String name);

    List<Product> getSortedProducts(String sort);

}
