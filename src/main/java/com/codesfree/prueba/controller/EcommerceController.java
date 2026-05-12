package com.codesfree.prueba.controller;

import com.codesfree.prueba.dto.SuperuserDashboardResponse;
import com.codesfree.prueba.dto.OrderDto;
import com.codesfree.prueba.model.Order;
import com.codesfree.prueba.model.Product;
import com.codesfree.prueba.model.ProductCategory;
import com.codesfree.prueba.service.EcommerceService;
import com.codesfree.prueba.service.SuperuserDashboardService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final SuperuserDashboardService superuserDashboardService;

    public EcommerceController(EcommerceService ecommerceService, SuperuserDashboardService superuserDashboardService) {
        this.ecommerceService = ecommerceService;
        this.superuserDashboardService = superuserDashboardService;
    }

    @GetMapping("/products")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN_TIENDA', 'ROLE_ENCARGADO_TIENDA')")
    public List<Product> getProducts() {
        return ecommerceService.getAllProducts();
    }

    @GetMapping("/products/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN_TIENDA', 'ROLE_ENCARGADO_TIENDA')")
    public Product getProduct(@PathVariable Long id) {
        return ecommerceService.getProduct(id);
    }

    @PostMapping("/products")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN_TIENDA', 'ROLE_ENCARGADO_TIENDA')")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ecommerceService.createProduct(product));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN_TIENDA', 'ROLE_ENCARGADO_TIENDA')")
    public List<ProductCategory> getCategories() {
        return ecommerceService.getCategories();
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN_TIENDA', 'ROLE_ENCARGADO_TIENDA')")
    public ResponseEntity<ProductCategory> createCategory(@RequestBody ProductCategory category) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ecommerceService.createCategory(category));
    }

    @PostMapping("/orders")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN_TIENDA', 'ROLE_ENCARGADO_TIENDA', 'ROLE_CLIENTE')")
    public ResponseEntity<Order> createOrder(@RequestBody OrderDto orderDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ecommerceService.createOrder(orderDto));
    }

    @GetMapping("/orders")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN_TIENDA', 'ROLE_ENCARGADO_TIENDA', 'ROLE_CLIENTE')")
    public List<Order> getOrders() {
        return ecommerceService.getAllOrders();
    }

    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    @GetMapping("/dashboard/superuser")
    public SuperuserDashboardResponse getSuperuserDashboard() {
        return superuserDashboardService.buildSummary();
    }
}
