package com.codesfree.prueba.controller;

import com.codesfree.prueba.dto.OrderDto;
import com.codesfree.prueba.model.Order;
import com.codesfree.prueba.model.Product;
import com.codesfree.prueba.model.ProductCategory;
import com.codesfree.prueba.service.EcommerceService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ecommerce")
public class EcommerceController {

    private final EcommerceService ecommerceService;

    public EcommerceController(EcommerceService ecommerceService) {
        this.ecommerceService = ecommerceService;
    }

    @GetMapping("/products")
    public List<Product> getProducts() {
        return ecommerceService.getAllProducts();
    }

    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable Long id) {
        return ecommerceService.getProduct(id);
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ecommerceService.createProduct(product));
    }

    @GetMapping("/categories")
    public List<ProductCategory> getCategories() {
        return ecommerceService.getCategories();
    }

    @PostMapping("/categories")
    public ResponseEntity<ProductCategory> createCategory(@RequestBody ProductCategory category) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ecommerceService.createCategory(category));
    }

    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@RequestBody OrderDto orderDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ecommerceService.createOrder(orderDto));
    }

    @GetMapping("/orders")
    public List<Order> getOrders() {
        return ecommerceService.getAllOrders();
    }
}

