package com.ecommerce.ecommerce_backend.service.impl;

import com.ecommerce.ecommerce_backend.model.Product;
import com.ecommerce.ecommerce_backend.repository.ProductRepository;
import com.ecommerce.ecommerce_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;


    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product updateProduct(Long id, Product product) {
        Product existingProduct = productRepository.findById(id).orElseThrow();

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setQty(product.getQty());
        existingProduct.setImageUrl(product.getImageUrl());

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product Not Found"));
        productRepository.delete(product);
    }

    public List<Product> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Product> getSortedProducts(String sort) {
        String[] parts = sort.split("_");
        if (parts.length != 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Sort Parameter Format. Use 'price_asc' or 'price_desc'.");
        }

        String field = parts[0];
        String order = parts[1];
        if (!field.equals("price")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only Sorting By 'price' Is Supported.");
        }

        if (order.equals("asc")) {
            return productRepository.findAllByOrderByPriceAsc();
        } else if (order.equals("desc")) {
            return productRepository.findAllByOrderByPriceDesc();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Sort Order. Use 'asc' or 'desc'.");
        }
    }

}
