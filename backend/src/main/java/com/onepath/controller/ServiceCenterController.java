package com.onepath.controller;

import com.onepath.entity.ServiceCenter;
import com.onepath.service.ServiceCenterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/service-centers", "/api/service-centres", "/service-centers", "/service-centres"})
public class ServiceCenterController {

    private final ServiceCenterService serviceCenterService;

    public ServiceCenterController(ServiceCenterService serviceCenterService) {
        this.serviceCenterService = serviceCenterService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceCenter>> getServiceCenters(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String serviceType) {
        List<ServiceCenter> list = serviceCenterService.getServiceCenters(search, city, category, serviceType);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceCenter> getServiceCenterById(@PathVariable Long id) {
        ServiceCenter center = serviceCenterService.getServiceCenterById(id);
        return ResponseEntity.ok(center);
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ServiceCenter> createServiceCenter(@Valid @RequestBody ServiceCenter serviceCenter) {
        ServiceCenter created = serviceCenterService.createServiceCenter(serviceCenter);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}
