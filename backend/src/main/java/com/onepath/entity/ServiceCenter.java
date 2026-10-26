package com.onepath.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_centers", indexes = {
    @Index(name = "idx_center_city", columnList = "city"),
    @Index(name = "idx_center_category", columnList = "category"),
    @Index(name = "idx_center_service_type", columnList = "service_type"),
    @Index(name = "idx_center_slug", columnList = "slug", unique = true)
})
public class ServiceCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String name;

    @Column(unique = true, length = 150)
    private String slug;

    @Column(length = 50)
    private String category; // PASSPORT, DRIVING_LICENCE, CERTIFICATES

    @Column(name = "service_type", length = 100)
    private String serviceType; // e.g. Regional Passport Office (RPO), Passport Seva Kendra (PSK), Post Office PSK (POPSK), Regional Transport Office (RTO), MeeSeva Government Centre, Grama/Ward Sachivalayam, Tahsildar Office, MeeSeva Authorized Franchise

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String locality; // e.g. Governorpet, Labbipet, Benz Circle, Auto Nagar

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String state;

    @Column(name = "pincode", length = 20)
    private String pincode;

    @Column(name = "contact_number", length = 100)
    private String contactNumber;

    @Column(length = 100)
    private String email;

    @Column(name = "operating_hours", length = 200)
    private String operatingHours;

    @Column(name = "supported_services", columnDefinition = "TEXT")
    private String supportedServices;

    @Column(name = "is_government", nullable = false)
    private Boolean isGovernment = true;

    @Column(name = "is_rpo", nullable = false)
    private Boolean isRpo = false;

    @Column(name = "official_website", length = 300)
    private String officialWebsite;

    @Column(name = "verification_status", length = 50)
    private String verificationStatus = "VERIFIED_OFFICIAL";

    @Column(name = "last_verified", length = 50)
    private String lastVerified = "October 2026";

    private Double latitude;

    private Double longitude;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ServiceCenter() {
    }

    public ServiceCenter(String name, String slug, String category, String serviceType,
                         String address, String locality, String city, String district, String state, String pincode,
                         String contactNumber, String operatingHours, String supportedServices,
                         Boolean isGovernment, Boolean isRpo, String officialWebsite,
                         Double latitude, Double longitude) {
        this.name = name;
        this.slug = slug;
        this.category = category;
        this.serviceType = serviceType;
        this.address = address;
        this.locality = locality;
        this.city = city;
        this.district = district;
        this.state = state;
        this.pincode = pincode;
        this.contactNumber = contactNumber;
        this.operatingHours = operatingHours;
        this.supportedServices = supportedServices;
        this.isGovernment = isGovernment != null ? isGovernment : true;
        this.isRpo = isRpo != null ? isRpo : false;
        this.officialWebsite = officialWebsite;
        this.latitude = latitude;
        this.longitude = longitude;
        this.verificationStatus = "VERIFIED_OFFICIAL";
        this.lastVerified = "October 2026";
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.verificationStatus == null) {
            this.verificationStatus = "VERIFIED_OFFICIAL";
        }
        if (this.lastVerified == null) {
            this.lastVerified = "October 2026";
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(String operatingHours) {
        this.operatingHours = operatingHours;
    }

    public String getSupportedServices() {
        return supportedServices;
    }

    public void setSupportedServices(String supportedServices) {
        this.supportedServices = supportedServices;
    }

    public Boolean getIsGovernment() {
        return isGovernment;
    }

    public void setIsGovernment(Boolean isGovernment) {
        this.isGovernment = isGovernment;
    }

    public Boolean getIsRpo() {
        return isRpo;
    }

    public void setIsRpo(Boolean isRpo) {
        this.isRpo = isRpo;
    }

    public String getOfficialWebsite() {
        return officialWebsite;
    }

    public void setOfficialWebsite(String officialWebsite) {
        this.officialWebsite = officialWebsite;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getLastVerified() {
        return lastVerified;
    }

    public void setLastVerified(String lastVerified) {
        this.lastVerified = lastVerified;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
