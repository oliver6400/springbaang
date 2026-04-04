package com.codesfree.prueba.controller;

import com.codesfree.prueba.model.Contact;
import com.codesfree.prueba.model.Customer;
import com.codesfree.prueba.model.Lead;
import com.codesfree.prueba.service.CrmService;
import java.util.List;
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
    public List<Customer> getCustomers() {
        return crmService.getAllCustomers();
    }

    @PostMapping("/customers")
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crmService.createCustomer(customer));
    }

    @GetMapping("/leads")
    public List<Lead> getLeads() {
        return crmService.getAllLeads();
    }

    @PostMapping("/leads")
    public ResponseEntity<Lead> createLead(@RequestBody Lead lead) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crmService.createLead(lead));
    }

    @GetMapping("/contacts")
    public List<Contact> getContacts() {
        return crmService.getAllContacts();
    }

    @PostMapping("/contacts")
    public ResponseEntity<Contact> createContact(@RequestBody Contact contact) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crmService.createContact(contact));
    }
}

