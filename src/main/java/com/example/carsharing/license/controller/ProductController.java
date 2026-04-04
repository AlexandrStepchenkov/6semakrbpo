package com.example.carsharing.license.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.carsharing.license.dto.ProductRequest;
import com.example.carsharing.license.model.LicenseProduct;
import com.example.carsharing.license.repository.LicenseProductRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final LicenseProductRepository productRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseProduct> createProduct(@RequestBody ProductRequest request) {
        LicenseProduct product = new LicenseProduct();
        product.setName(request.getName());
        product.setBlocked(Boolean.TRUE.equals(request.getIsBlocked()));
        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(product));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LicenseProduct>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LicenseProduct> getProductById(@PathVariable UUID id) {
        Optional<LicenseProduct> product = productRepository.findById(id);
        return product.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseProduct> updateProduct(@PathVariable UUID id, @RequestBody ProductRequest request) {
        Optional<LicenseProduct> productOpt = productRepository.findById(id);

        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        LicenseProduct product = productOpt.get();
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getIsBlocked() != null) {
            product.setBlocked(request.getIsBlocked());
        }

        return ResponseEntity.ok(productRepository.save(product));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
