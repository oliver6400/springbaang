package com.codesfree.prueba.service;

import com.codesfree.prueba.dto.SuperuserDashboardResponse;
import com.codesfree.prueba.model.Contact;
import com.codesfree.prueba.model.Customer;
import com.codesfree.prueba.model.Lead;
import com.codesfree.prueba.model.LeadStatus;
import com.codesfree.prueba.model.Order;
import com.codesfree.prueba.model.OrderStatus;
import com.codesfree.prueba.model.Product;
import com.codesfree.prueba.model.ProductCategory;
import com.codesfree.prueba.model.Tenant;
import com.codesfree.prueba.repository.ContactRepository;
import com.codesfree.prueba.repository.CustomerRepository;
import com.codesfree.prueba.repository.LeadRepository;
import com.codesfree.prueba.repository.OrderRepository;
import com.codesfree.prueba.repository.ProductCategoryRepository;
import com.codesfree.prueba.repository.ProductRepository;
import com.codesfree.prueba.repository.TenantRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SuperuserDashboardService {

    private final TenantRepository tenantRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ContactRepository contactRepository;
    private final LeadRepository leadRepository;

    public SuperuserDashboardService(
            TenantRepository tenantRepository,
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository,
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            ContactRepository contactRepository,
            LeadRepository leadRepository) {
        this.tenantRepository = tenantRepository;
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.contactRepository = contactRepository;
        this.leadRepository = leadRepository;
    }

    @Transactional(readOnly = true)
    public SuperuserDashboardResponse buildSummary() {
        List<Tenant> tenants = tenantRepository.findAll();
        List<Product> products = productRepository.findAll();
        List<ProductCategory> categories = productCategoryRepository.findAll();
        List<Order> orders = orderRepository.findAll();
        List<Customer> customers = customerRepository.findAll();
        List<Contact> contacts = contactRepository.findAll();
        List<Lead> leads = leadRepository.findAll();

        BigDecimal grossRevenue = orders.stream()
                .map(Order::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingOrders = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PENDING)
                .count();

        long qualifiedLeads = leads.stream()
                .filter(lead -> lead.getStatus() == LeadStatus.QUALIFIED || lead.getStatus() == LeadStatus.CONVERTED)
                .count();

        SuperuserDashboardResponse.Overview overview = new SuperuserDashboardResponse.Overview(
                tenants.size(),
                products.size(),
                categories.size(),
                orders.size(),
                customers.size(),
                contacts.size(),
                leads.size(),
                pendingOrders,
                qualifiedLeads,
                grossRevenue);

        List<SuperuserDashboardResponse.InsightCard> commerceHighlights = List.of(
                new SuperuserDashboardResponse.InsightCard(
                        "Ingreso bruto",
                        formatCurrency(grossRevenue),
                        "Facturación total consolidada de todos los tenants ecommerce.",
                        "emerald"),
                new SuperuserDashboardResponse.InsightCard(
                        "Órdenes pendientes",
                        String.valueOf(pendingOrders),
                        "Pedidos que aún requieren seguimiento operativo o logístico.",
                        "amber"),
                new SuperuserDashboardResponse.InsightCard(
                        "Catálogo activo",
                        products.size() + " productos",
                        "Inventario publicado actualmente en toda la plataforma.",
                        "sky"));

        List<SuperuserDashboardResponse.InsightCard> crmHighlights = List.of(
                new SuperuserDashboardResponse.InsightCard(
                        "Pipeline calificado",
                        String.valueOf(qualifiedLeads),
                        "Leads en estado QUALIFIED o CONVERTED listos para capitalizar.",
                        "violet"),
                new SuperuserDashboardResponse.InsightCard(
                        "Clientes con contacto",
                        String.valueOf(countCustomersWithContacts(contacts)),
                        "Clientes que ya cuentan con al menos un contacto registrado en CRM.",
                        "rose"),
                new SuperuserDashboardResponse.InsightCard(
                        "Cobertura CRM",
                        formatPercentage(customers.isEmpty() ? 0D : (double) contacts.size() / customers.size()),
                        "Relación de contactos cargados respecto a la base total de clientes.",
                        "slate"));

        List<SuperuserDashboardResponse.TenantActivity> tenantActivity = tenants.stream()
                .map(tenant -> buildTenantActivity(tenant, orders, customers, leads))
                .sorted(Comparator.comparing(SuperuserDashboardResponse.TenantActivity::getRevenue).reversed())
                .limit(5)
                .toList();

        List<SuperuserDashboardResponse.PipelineStage> pipeline = List.of(LeadStatus.values()).stream()
                .map(status -> new SuperuserDashboardResponse.PipelineStage(
                        status.name(),
                        leads.stream().filter(lead -> lead.getStatus() == status).count()))
                .toList();

        List<SuperuserDashboardResponse.RecentOrder> recentOrders = orders.stream()
                .sorted(Comparator.comparing(Order::getOrderDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .map(order -> new SuperuserDashboardResponse.RecentOrder(
                        order.getId(),
                        order.getTenant().getName(),
                        order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName(),
                        order.getStatus().name(),
                        order.getTotal(),
                        order.getOrderDate()))
                .toList();

        return new SuperuserDashboardResponse(
                overview,
                commerceHighlights,
                crmHighlights,
                tenantActivity,
                pipeline,
                recentOrders);
    }

    private SuperuserDashboardResponse.TenantActivity buildTenantActivity(
            Tenant tenant,
            List<Order> orders,
            List<Customer> customers,
            List<Lead> leads) {
        long tenantOrders = orders.stream().filter(order -> order.getTenant().getId().equals(tenant.getId())).count();
        long tenantCustomers = customers.stream().filter(customer -> customer.getTenant().getId().equals(tenant.getId())).count();
        long tenantLeads = leads.stream().filter(lead -> lead.getTenant().getId().equals(tenant.getId())).count();
        BigDecimal revenue = orders.stream()
                .filter(order -> order.getTenant().getId().equals(tenant.getId()))
                .map(Order::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SuperuserDashboardResponse.TenantActivity(
                tenant.getName(),
                tenantOrders,
                tenantCustomers,
                tenantLeads,
                revenue);
    }

    private long countCustomersWithContacts(List<Contact> contacts) {
        return contacts.stream()
                .filter(contact -> contact.getCustomer() != null)
                .map(contact -> contact.getCustomer().getId())
                .distinct()
                .count();
    }

    private String formatCurrency(BigDecimal value) {
        return "$" + value.setScale(2, RoundingMode.HALF_UP);
    }

    private String formatPercentage(double ratio) {
        return String.format("%.0f%%", ratio * 100);
    }
}
