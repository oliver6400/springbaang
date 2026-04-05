package com.codesfree.prueba.service;

import com.codesfree.prueba.dto.OrderDto;
import com.codesfree.prueba.exception.ResourceNotFoundException;
import com.codesfree.prueba.model.Customer;
import com.codesfree.prueba.model.Order;
import com.codesfree.prueba.model.OrderItem;
import com.codesfree.prueba.model.OrderStatus;
import com.codesfree.prueba.model.PaymentStatus;
import com.codesfree.prueba.model.Product;
import com.codesfree.prueba.model.ProductCategory;
import com.codesfree.prueba.model.Tenant;
import com.codesfree.prueba.repository.CustomerRepository;
import com.codesfree.prueba.repository.OrderRepository;
import com.codesfree.prueba.repository.ProductCategoryRepository;
import com.codesfree.prueba.repository.ProductRepository;
import com.codesfree.prueba.repository.TenantRepository;
import com.codesfree.prueba.tenant.TenantContext;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EcommerceService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final TenantRepository tenantRepository;

    public EcommerceService(
            ProductRepository productRepository,
            ProductCategoryRepository categoryRepository,
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            TenantRepository tenantRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.tenantRepository = tenantRepository;
    }

    private Long getCurrentTenantId() {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Missing tenant header X-Tenant-Id");
        }
        return tenantId;
    }

    private Tenant findTenant() {
        return tenantRepository.findById(getCurrentTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found for current request"));
    }

    public Product createProduct(Product product) {
        product.setTenant(findTenant());
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            ProductCategory category = categoryRepository.findById(product.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + product.getCategory().getId()));
            product.setCategory(category);
        }
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findByTenantId(getCurrentTenantId());
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .filter(product -> product.getTenant().getId().equals(getCurrentTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    public ProductCategory createCategory(ProductCategory category) {
        category.setTenant(findTenant());
        return categoryRepository.save(category);
    }

    public List<ProductCategory> getCategories() {
        return categoryRepository.findByTenantId(getCurrentTenantId());
    }

    public Order createOrder(OrderDto orderDto) {
        Customer customer = customerRepository.findById(orderDto.getCustomerId())
                .filter(c -> c.getTenant().getId().equals(getCurrentTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + orderDto.getCustomerId()));

        Order order = new Order();
        order.setTenant(findTenant());
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);

        for (OrderDto.OrderItemDto itemDto : orderDto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .filter(p -> p.getTenant().getId().equals(getCurrentTenantId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemDto.getProductId()));
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.recalculateTotal();
            order.addItem(item);
            if (product.getStock() != null) {
                product.setStock(Math.max(product.getStock() - item.getQuantity(), 0));
                productRepository.save(product);
            }
        }

        if (order.getSubtotal() == null || order.getSubtotal().compareTo(BigDecimal.ZERO) <= 0) {
            order.setSubtotal(order.getItems().stream()
                    .map(OrderItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        order.setTotal(order.getSubtotal().add(order.getTaxAmount()).add(order.getShippingAmount()));

        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findByTenantId(getCurrentTenantId());
    }
}

