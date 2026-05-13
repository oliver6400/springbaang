package com.codesfree.prueba.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class SuperuserDashboardResponse {

    private final Overview overview;
    private final List<InsightCard> commerceHighlights;
    private final List<InsightCard> crmHighlights;
    private final List<TenantActivity> tenantActivity;
    private final List<PipelineStage> pipeline;
    private final List<RecentOrder> recentOrders;

    public SuperuserDashboardResponse(
            Overview overview,
            List<InsightCard> commerceHighlights,
            List<InsightCard> crmHighlights,
            List<TenantActivity> tenantActivity,
            List<PipelineStage> pipeline,
            List<RecentOrder> recentOrders) {
        this.overview = overview;
        this.commerceHighlights = commerceHighlights;
        this.crmHighlights = crmHighlights;
        this.tenantActivity = tenantActivity;
        this.pipeline = pipeline;
        this.recentOrders = recentOrders;
    }

    public Overview getOverview() {
        return overview;
    }

    public List<InsightCard> getCommerceHighlights() {
        return commerceHighlights;
    }

    public List<InsightCard> getCrmHighlights() {
        return crmHighlights;
    }

    public List<TenantActivity> getTenantActivity() {
        return tenantActivity;
    }

    public List<PipelineStage> getPipeline() {
        return pipeline;
    }

    public List<RecentOrder> getRecentOrders() {
        return recentOrders;
    }

    public static class Overview {
        private final long tenants;
        private final long products;
        private final long categories;
        private final long orders;
        private final long customers;
        private final long contacts;
        private final long leads;
        private final long pendingOrders;
        private final long qualifiedLeads;
        private final BigDecimal grossRevenue;

        public Overview(
                long tenants,
                long products,
                long categories,
                long orders,
                long customers,
                long contacts,
                long leads,
                long pendingOrders,
                long qualifiedLeads,
                BigDecimal grossRevenue) {
            this.tenants = tenants;
            this.products = products;
            this.categories = categories;
            this.orders = orders;
            this.customers = customers;
            this.contacts = contacts;
            this.leads = leads;
            this.pendingOrders = pendingOrders;
            this.qualifiedLeads = qualifiedLeads;
            this.grossRevenue = grossRevenue;
        }

        public long getTenants() {
            return tenants;
        }

        public long getProducts() {
            return products;
        }

        public long getCategories() {
            return categories;
        }

        public long getOrders() {
            return orders;
        }

        public long getCustomers() {
            return customers;
        }

        public long getContacts() {
            return contacts;
        }

        public long getLeads() {
            return leads;
        }

        public long getPendingOrders() {
            return pendingOrders;
        }

        public long getQualifiedLeads() {
            return qualifiedLeads;
        }

        public BigDecimal getGrossRevenue() {
            return grossRevenue;
        }
    }

    public static class InsightCard {
        private final String title;
        private final String value;
        private final String description;
        private final String tone;

        public InsightCard(String title, String value, String description, String tone) {
            this.title = title;
            this.value = value;
            this.description = description;
            this.tone = tone;
        }

        public String getTitle() {
            return title;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public String getTone() {
            return tone;
        }
    }

    public static class TenantActivity {
        private final String tenant;
        private final long orders;
        private final long customers;
        private final long leads;
        private final BigDecimal revenue;

        public TenantActivity(String tenant, long orders, long customers, long leads, BigDecimal revenue) {
            this.tenant = tenant;
            this.orders = orders;
            this.customers = customers;
            this.leads = leads;
            this.revenue = revenue;
        }

        public String getTenant() {
            return tenant;
        }

        public long getOrders() {
            return orders;
        }

        public long getCustomers() {
            return customers;
        }

        public long getLeads() {
            return leads;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }
    }

    public static class PipelineStage {
        private final String status;
        private final long total;

        public PipelineStage(String status, long total) {
            this.status = status;
            this.total = total;
        }

        public String getStatus() {
            return status;
        }

        public long getTotal() {
            return total;
        }
    }

    public static class RecentOrder {
        private final Long orderId;
        private final String tenant;
        private final String customer;
        private final String status;
        private final BigDecimal total;
        private final Instant orderDate;

        public RecentOrder(Long orderId, String tenant, String customer, String status, BigDecimal total, Instant orderDate) {
            this.orderId = orderId;
            this.tenant = tenant;
            this.customer = customer;
            this.status = status;
            this.total = total;
            this.orderDate = orderDate;
        }

        public Long getOrderId() {
            return orderId;
        }

        public String getTenant() {
            return tenant;
        }

        public String getCustomer() {
            return customer;
        }

        public String getStatus() {
            return status;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public Instant getOrderDate() {
            return orderDate;
        }
    }
}
