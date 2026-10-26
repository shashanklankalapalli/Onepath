package com.onepath.service;

import com.onepath.entity.ServiceCenter;
import com.onepath.exception.ResourceNotFoundException;
import com.onepath.repository.ServiceCenterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ServiceCenterServiceTest {

    private ServiceCenterRepository serviceCenterRepository;
    private ServiceCenterService serviceCenterService;

    @BeforeEach
    void setUp() {
        serviceCenterRepository = mock(ServiceCenterRepository.class);
        serviceCenterService = new ServiceCenterService(serviceCenterRepository);
    }

    @Test
    @DisplayName("Should retrieve centres with category and city filters")
    void testGetCentersWithFilters() {
        ServiceCenter center = new ServiceCenter(
                "Passport Seva Kendra (PSK) Vijayawada",
                "psk-vijayawada",
                "PASSPORT",
                "Passport Seva Kendra (PSK)",
                "M.G. Road, Labbipet, Vijayawada",
                "Labbipet",
                "Vijayawada",
                "NTR District",
                "Andhra Pradesh",
                "520010",
                "1800-258-1800",
                "Mon - Fri: 9:00 AM - 5:30 PM",
                "Fresh Passport, Renewal, Tatkaal",
                true,
                false,
                "https://www.passportindia.gov.in",
                16.5042,
                80.6433
        );

        when(serviceCenterRepository.findWithFilters(eq("PASSPORT"), eq("Vijayawada"), isNull(), isNull()))
                .thenReturn(List.of(center));

        List<ServiceCenter> result = serviceCenterService.getServiceCenters(null, "Vijayawada", "PASSPORT", "ALL");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("psk-vijayawada", result.get(0).getSlug());
        assertEquals("Vijayawada", result.get(0).getCity());
        assertTrue(result.get(0).getIsGovernment());
        assertFalse(result.get(0).getIsRpo());
    }

    @Test
    @DisplayName("Should find centre by ID")
    void testFindById() {
        ServiceCenter center = new ServiceCenter();
        center.setId(10L);
        center.setName("RTO Vijayawada");

        when(serviceCenterRepository.findById(10L)).thenReturn(Optional.of(center));

        ServiceCenter found = serviceCenterService.getServiceCenterById(10L);
        assertNotNull(found);
        assertEquals("RTO Vijayawada", found.getName());
    }

    @Test
    @DisplayName("Should throw exception when centre ID not found")
    void testFindByIdNotFound() {
        when(serviceCenterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            serviceCenterService.getServiceCenterById(999L);
        });
    }

    @Test
    @DisplayName("Should find centre by slug")
    void testFindBySlug() {
        ServiceCenter center = new ServiceCenter();
        center.setSlug("rpo-vijayawada");
        center.setName("Regional Passport Office Vijayawada");
        center.setIsRpo(true);

        when(serviceCenterRepository.findBySlug("rpo-vijayawada")).thenReturn(Optional.of(center));

        ServiceCenter found = serviceCenterService.getServiceCenterBySlug("rpo-vijayawada");
        assertNotNull(found);
        assertTrue(found.getIsRpo());
    }

    @Test
    @DisplayName("Should invoke real findWithFilters default method and delegate to findAll(Specification)")
    void testFindWithFiltersRealDefaultMethod() {
        doCallRealMethod().when(serviceCenterRepository).findWithFilters(any(), any(), any(), any());
        when(serviceCenterRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of(new ServiceCenter()));

        List<ServiceCenter> allNull = serviceCenterRepository.findWithFilters(null, null, null, null);
        assertNotNull(allNull);
        assertEquals(1, allNull.size());

        List<ServiceCenter> withFilters = serviceCenterRepository.findWithFilters("PASSPORT", "Vijayawada", "PSK", "Passport");
        assertNotNull(withFilters);
        assertEquals(1, withFilters.size());

        verify(serviceCenterRepository, times(2)).findAll(any(org.springframework.data.jpa.domain.Specification.class));
    }
}
