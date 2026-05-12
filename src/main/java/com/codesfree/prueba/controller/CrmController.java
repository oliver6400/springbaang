package com.codesfree.prueba.controller;

import com.codesfree.prueba.model.Contact;
import com.codesfree.prueba.model.Customer;
import com.codesfree.prueba.model.Lead;
import com.codesfree.prueba.service.CrmService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crm")
public class CrmController {

    private final CrmService crmService;

    public CrmController(CrmService crmService) {
        this.crmService = crmService;
    }

    @GetMapping("/customers")
    @PreAuthorize("hasAnyAuthority('ROLE_DUENO_EMPRESA', 'ROLE_ADMIN_EMPRESA', 'ROLE_ADMIN_TIENDA', 'ROLE_ENCARGADO_TIENDA', 'ROLE_SOPORTE')")
    public List<Customer> getCustomers() {
        return crmService.getAllCustomers();
    }

    @PostMapping("/customers")
    @PreAuthorize("hasAnyAuthority('ROLE_DUENO_EMPRESA', 'ROLE_ADMIN_EMPRESA', 'ROLE_ADMIN_TIENDA', 'ROLE_ENCARGADO_TIENDA', 'ROLE_SOPORTE')")
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crmService.createCustomer(customer));
    }

    @GetMapping("/leads")
    @PreAuthorize("hasAnyAuthority('ROLE_DUENO_EMPRESA', 'ROLE_ADMIN_EMPRESA', 'ROLE_ADMIN_TIENDA', 'ROLE_ENCARGADO_TIENDA', 'ROLE_SOPORTE')")
    public List<Lead> getLeads() {
        return crmService.getAllLeads();
    }

    @PostMapping("/leads")
    @PreAuthorize("hasAnyAuthority('ROLE_DUENO_EMPRESA', 'ROLE_ADMIN_EMPRESA', 'ROLE_ADMIN_TIENDA', 'ROLE_ENCARGADO_TIENDA', 'ROLE_SOPORTE')")
    public ResponseEntity<Lead> createLead(@RequestBody Lead lead) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crmService.createLead(lead));
    }

    @GetMapping("/contacts")
    @PreAuthorize("hasAnyAuthority('ROLE_DUENO_EMPRESA', 'ROLE_ADMIN_EMPRESA', 'ROLE_ADMIN_TIENDA', 'ROLE_ENCARGADO_TIENDA', 'ROLE_SOPORTE')")
    public List<Contact> getContacts() {
        return crmService.getAllContacts();
    }

    @PostMapping("/contacts")
    @PreAuthorize("hasAnyAuthority('ROLE_DUENO_EMPRESA', 'ROLE_ADMIN_EMPRESA', 'ROLE_ADMIN_TIENDA', 'ROLE_ENCARGADO_TIENDA', 'ROLE_SOPORTE')")
    public ResponseEntity<Contact> createContact(@RequestBody Contact contact) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crmService.createContact(contact));
    }
}
