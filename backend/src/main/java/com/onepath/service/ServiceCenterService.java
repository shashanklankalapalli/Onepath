package com.onepath.service;

import com.onepath.entity.ServiceCenter;
import com.onepath.exception.ResourceNotFoundException;
import com.onepath.repository.ServiceCenterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiceCenterService {

    private final ServiceCenterRepository serviceCenterRepository;

    public ServiceCenterService(ServiceCenterRepository serviceCenterRepository) {
        this.serviceCenterRepository = serviceCenterRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceCenter> getServiceCenters(String search, String city, String category, String serviceType) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String cleanCity = (city != null && !city.trim().isEmpty() && !"ALL".equalsIgnoreCase(city.trim())) ? city.trim() : null;
        String cleanCategory = (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category.trim())) ? category.trim() : null;
        String cleanServiceType = (serviceType != null && !serviceType.trim().isEmpty() && !"ALL".equalsIgnoreCase(serviceType.trim())) ? serviceType.trim() : null;

        return serviceCenterRepository.findWithFilters(cleanCategory, cleanCity, cleanServiceType, cleanSearch);
    }

    @Transactional(readOnly = true)
    public ServiceCenter getServiceCenterById(Long id) {
        return serviceCenterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service centre not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public ServiceCenter getServiceCenterBySlug(String slug) {
        return serviceCenterRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Service centre not found with slug: " + slug));
    }

    @Transactional
    public ServiceCenter createServiceCenter(ServiceCenter serviceCenter) {
        return serviceCenterRepository.save(serviceCenter);
    }
}
