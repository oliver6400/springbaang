package com.codesfree.prueba.service;

import com.codesfree.prueba.exception.ResourceNotFoundException;
import com.codesfree.prueba.model.Contact;
import com.codesfree.prueba.model.Customer;
import com.codesfree.prueba.model.Lead;
import com.codesfree.prueba.model.Tenant;
import com.codesfree.prueba.repository.ContactRepository;
import com.codesfree.prueba.repository.CustomerRepository;
import com.codesfree.prueba.repository.LeadRepository;
import com.codesfree.prueba.repository.TenantRepository;
import com.codesfree.prueba.tenant.TenantContext;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CrmService {

    private final CustomerRepository customerRepository;
    private final LeadRepository leadRepository;
    private final ContactRepository contactRepository;
    private final TenantRepository tenantRepository;

    public CrmService(
            CustomerRepository customerRepository,
            LeadRepository leadRepository,
            ContactRepository contactRepository,
            TenantRepository tenantRepository) {
        this.customerRepository = customerRepository;
        this.leadRepository = leadRepository;
        this.contactRepository = contactRepository;
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

    public Customer createCustomer(Customer customer) {
        customer.setTenant(findTenant());
        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findByTenantId(getCurrentTenantId());
    }

    public Lead createLead(Lead lead) {
        lead.setTenant(findTenant());
        if (lead.getCustomer() != null && lead.getCustomer().getId() != null) {
            Customer customer = customerRepository.findById(lead.getCustomer().getId())
                    .filter(c -> c.getTenant().getId().equals(getCurrentTenantId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + lead.getCustomer().getId()));
            lead.setCustomer(customer);
        }
        return leadRepository.save(lead);
    }

    public List<Lead> getAllLeads() {
        return leadRepository.findByTenantId(getCurrentTenantId());
    }

    public Contact createContact(Contact contact) {
        contact.setTenant(findTenant());
        if (contact.getCustomer() != null && contact.getCustomer().getId() != null) {
            Customer customer = customerRepository.findById(contact.getCustomer().getId())
                    .filter(c -> c.getTenant().getId().equals(getCurrentTenantId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + contact.getCustomer().getId()));
            contact.setCustomer(customer);
        }
        return contactRepository.save(contact);
    }

    public List<Contact> getAllContacts() {
        return contactRepository.findByTenantId(getCurrentTenantId());
    }
}

